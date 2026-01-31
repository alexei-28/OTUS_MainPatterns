package ru.otus.main_patterns.hw05.commands;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import ru.otus.main_patterns.hw05.core.IoC;

class RegisterDependencyCommandTest {
  @Mock private Function<Object[], Object> strategyMock;
  private Map<String, Function<Object[], Object>> scopeMap;

  @BeforeEach
  void setUp() {
    scopeMap = new HashMap<>();
  }

  @Test
  @DisplayName("Should fetch current scope from IoC and register the dependency")
  void shouldExecuteRegisterDependencyInCurrentScope() {
    // We use try-with-resources to mock the static IoC.resolve call
    try (MockedStatic<IoC> iocMock = mockStatic(IoC.class)) {
      // Arrange
      iocMock.when(() -> IoC.resolve("ioc.scope.current")).thenReturn(scopeMap);
      String dependencyName = "MyService";
      RegisterDependencyCommand command =
          new RegisterDependencyCommand(dependencyName, strategyMock);
      // Act
      command.execute();
      // Assert
      assertTrue(scopeMap.containsKey(dependencyName), "Scope should contain the dependency key");
      assertEquals(
          strategyMock, scopeMap.get(dependencyName), "Strategy in scope should match the input");
      iocMock.verify(() -> IoC.resolve("ioc.scope.current"));
    }
  }
}
