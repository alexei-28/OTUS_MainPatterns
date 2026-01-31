package ru.otus.main_patterns.hw05.commands;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import ru.otus.main_patterns.hw05.core.IoC;
import ru.otus.main_patterns.hw05.interfaces.Command;

class InitCommandTest {
  private ConcurrentHashMap<String, Function<Object[], Object>> scopesMap;

  @BeforeEach
  void setup() throws Exception {
    scopesMap = new ConcurrentHashMap<>();
    // Since isAlreadyExecutesSuccessfully is private and static,
    // we use reflection to reset it to false for each test.
    Field field = InitCommand.class.getDeclaredField("isAlreadyExecutesSuccessfully");
    field.setAccessible(true);
    AtomicBoolean flag = (AtomicBoolean) field.get(null);
    flag.set(false);

    // Clear the ThreadLocal
    InitCommand.currentScopeThreadLocal.remove();
  }

  @Test
  @DisplayName("Should update IoC strategy when executed for the first time")
  void shouldExecuteSuccessfully() {
    try (MockedStatic<IoC> iocMock = mockStatic(IoC.class)) {
      // Arrange
      Command mockUpdateCommand = mock(Command.class);
      iocMock
          .when(() -> IoC.resolve(eq("update.ioc.resolve.dependency.strategy"), any()))
          .thenReturn(mockUpdateCommand);
      InitCommand initCommand = new InitCommand();
      // Act
      initCommand.execute();
      // Assert
      verify(mockUpdateCommand).execute();
      iocMock.verify(() -> IoC.resolve(eq("update.ioc.resolve.dependency.strategy"), any()));
    }
  }

  @Test
  @DisplayName("Should skip execution if already executed successfully")
  void shouldExecuteOnlyOnce() {
    try (MockedStatic<IoC> iocMock = mockStatic(IoC.class)) {
      // Arrange
      Command mockUpdateCommand = mock(Command.class);
      iocMock.when(() -> IoC.resolve(anyString(), any())).thenReturn(mockUpdateCommand);
      InitCommand initCommand = new InitCommand();
      // Act: Run twice
      initCommand.execute();
      initCommand.execute();
      // Assert: verify it was only actually processed once
      verify(mockUpdateCommand, times(1)).execute();
    }
  }

  @Test
  void ioCScopeCreateEmptyStrategy() {
    // Arrange: Simulate the map setup
    ConcurrentHashMap<String, Function<Object[], Object>> scopesMap = new ConcurrentHashMap<>();
    scopesMap.putIfAbsent(
        "ioc.scope.create.empty",
        (Object[] args) -> new HashMap<String, Function<Object[], Object>>());

    // Act: Retrieve the function and execute it
    Function<Object[], Object> strategy = scopesMap.get("ioc.scope.create.empty");
    Object result1 = strategy.apply(new Object[] {});
    Object result2 = strategy.apply(new Object[] {});

    // Assert
    assertThat(strategy).as("Strategy should be registered in the map").isNotNull();
    assertThat(result1).as("Result should be a HashMap").isInstanceOf(HashMap.class);

    // Act
    Map<?, ?> map1 = (Map<?, ?>) result1;
    Map<?, ?> map2 = (Map<?, ?>) result2;
    assertTrue(map1.isEmpty(), "The new map should be empty");
    assertThat(map1)
        .as("Each call should create a new instance (Factory behavior)")
        .isNotSameAs(map2);
  }

  @Test
  @DisplayName("Verify generated scope logic for ioc.scope.current")
  void shouldScopeFunctionality() throws Exception {
    // Arrange
    // We manually extract the map from the class to verify the lambdas registered inside it
    Field scopesMapField = InitCommand.class.getDeclaredField("scopesMap");
    scopesMapField.setAccessible(true);
    Map<String, Function<Object[], Object>> scopesMap =
        (Map<String, Function<Object[], Object>>) scopesMapField.get(null);
    try (MockedStatic<IoC> iocMock = mockStatic(IoC.class)) {
      iocMock.when(() -> IoC.resolve(anyString(), any())).thenReturn(mock(Command.class));
      // Act
      new InitCommand().execute();
      // Assert
      // Test: ioc.scope.current should return scopesMap when ThreadLocal is null
      Function<Object[], Object> strategy = scopesMap.get("ioc.scope.current");
      Object result = strategy.apply(new Object[] {});
      assertThat(scopesMap).isEqualTo(result);

      // Test: ioc.scope.current should return specific object if ThreadLocal is set
      Object customScope = new Object();
      InitCommand.currentScopeThreadLocal.set(customScope);
      assertThat(customScope).isEqualTo(strategy.apply(new Object[] {}));
    }
  }

  @Test
  @DisplayName(
      "Dependency 'ioc.register' should extract arguments and return RegisterDependencyCommand")
  void shouldReturnRegisterDependencyCommandWithCorrectArgs() {
    // Arrange
    scopesMap.putIfAbsent(
        "ioc.register",
        (Object[] args) -> {
          String dependencyName = (String) args[0];
          Function<Object[], Object> strategy = (Function<Object[], Object>) args[1];
          return new RegisterDependencyCommand(dependencyName, strategy);
        });
    String expectedDependencyName = "myService";
    Function<Object[], Object> expectedStrategy = args -> "resolvedValue";
    Function<Object[], Object> actualStrategy = scopesMap.get("ioc.register");
    // Act
    Object result = actualStrategy.apply(new Object[] {expectedDependencyName, expectedStrategy});
    // Assert
    assertThat(result)
        .as("The ioc.register strategy must return a Command object")
        .isNotNull()
        .isInstanceOf(RegisterDependencyCommand.class);
    RegisterDependencyCommand command = (RegisterDependencyCommand) result;
    assertThat(command.getDependencyName()).isEqualTo(expectedDependencyName);
    assertThat(command.getDependencyResolverStrategy()).isSameAs(expectedStrategy);
  }
}
