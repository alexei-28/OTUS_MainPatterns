package ru.otus.main_patterns.hw05.core;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.apache.commons.beanutils.ConstructorUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.BeforeEach;
import ru.otus.main_patterns.hw05.commands.UpdateIocResolveDependencyStrategyCommand;
import ru.otus.main_patterns.hw05.interfaces.Command;
import ru.otus.main_patterns.hw05.model.User;

/**
 * <a
 * href="https://github.com/etyumentcev/appserver/blob/main/appserver/core.tests/IocTests.cs">Help
 * tests</a>
 */
class IoCTest {
  private BiFunction<String, Object[], Object> originalStrategy;

  @BeforeEach
  void saveState() {
    // Save the real default strategy to restore after each test
    originalStrategy = IoC.strategy;
  }

  @AfterEach
  void restoreState() {
    IoC.strategy = originalStrategy;
  }

  @Test
  @DisplayName("Should delegate resolution to a mocked strategy")
  void shouldResolveWithMockedStrategy() {
    // Arrange
    BiFunction<String, Object[], Object> mockStrategy = mock(BiFunction.class);
    String dependency = "test.service";
    String expectedValue = "Hello World";
    when(mockStrategy.apply(eq(dependency), any())).thenReturn(expectedValue);
    // Swap the static strategy for our mock
    IoC.strategy = mockStrategy;
    // Act
    String actualValue = IoC.resolve(dependency);
    // Assert
    assertEquals(expectedValue, actualValue);
    verify(mockStrategy).apply(eq(dependency), any());
  }

  @Test
  @DisplayName("IoC should throw IllegalArgumentException for unknown dependency")
  void shouldThrowExceptionWhenUnknownDependency() {
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          IoC.resolve("unknown.dependency");
        },
        "Should throw exception when dependency is not found in default strategy");
  }

  @Test
  @DisplayName("IoC should throw ClassCastException when dependency resolves another type")
  void shouldThrowClassCastExceptionIfDependencyResolvesAnotherType() {
    assertThrows(
        ClassCastException.class,
        () -> {
          IoC.resolve(
              "update.ioc.resolve.dependency.strategy",
              (BiFunction<String, Object[], Object>) (dependency, args) -> args);
        });
  }

  @Test
  @DisplayName("IoC should return UpdateIocResolveDependencyStrategyCommand for default dependency")
  void shouldResolveCommandUpdateIocResolveDependencyStrategyCommand() {
    // Act
    Command command =
        IoC.resolve(
            "update.ioc.resolve.dependency.strategy",
            (Function<BiFunction<String, Object[], Object>, BiFunction<String, Object[], Object>>)
                stringObjectBiFunction -> null);
    // Assert
    assertThat(command).isNotNull();
    assertThat(command).isInstanceOf(UpdateIocResolveDependencyStrategyCommand.class);
  }

  @Test
  @DisplayName("IoC should update resolve dependency strategy")
  void shouldUpdateResolveDependencyStrategy() {
    // Arrange
    final boolean[] wasCalled = {false};
    // Act
    Command command =
        IoC.resolve(
            "update.ioc.resolve.dependency.strategy",
            (Function<BiFunction<String, Object[], Object>, BiFunction<String, Object[], Object>>)
                args -> {
                  wasCalled[0] = true;
                  return args;
                });
    command.execute();
    // Assert
    assertThat(wasCalled[0]).isTrue();
  }

  @Test
  @DisplayName("Should create instance of User when IoC resolve exist dependency")
  void shouldCreateInstanceOfUserByIoC() {
    // Arrange
    // Создаем хранилище для будущих стратегий
    ConcurrentHashMap<String, Function<Object[], Object>> scopesMap = new ConcurrentHashMap<>();
    // Обновляем базовую стратегию("update.ioc.resolve.dependency.strategy") IoC, чтобы она умела
    // искать в нашем scopesMap.
    UpdateIocResolveDependencyStrategyCommand updateCmd =
        IoC.resolve(
            "update.ioc.resolve.dependency.strategy",
            (Function<BiFunction<String, Object[], Object>, BiFunction<String, Object[], Object>>)
                oldStrategy ->
                    (dependency, args) -> {
                      if (scopesMap.containsKey(dependency)) {
                        Function<Object[], Object> strategy = scopesMap.get(dependency);
                        return strategy.apply(args);
                      }
                      return oldStrategy.apply(dependency, args);
                    });
    updateCmd.execute();
    // Регистрируем стратегию создания User
    String dependencyName = "ru.otus.main_patterns.hw05.model.User";
    scopesMap.put(
        dependencyName,
        args -> {
          try {
            return ConstructorUtils.invokeConstructor(Class.forName(dependencyName), args);
          } catch (Exception e) {
            throw new RuntimeException("Failed to create instance of " + dependencyName, e);
          }
        });

    //  Act: Вызываем resolve для User
    String userName = "John";
    int userAge = 30;
    User userFirst = IoC.resolve(dependencyName, userName, userAge);
    //  Assert
    assertThat(userFirst)
        .as("IoC should return a non-null User instance")
        .isNotNull()
        .isInstanceOf(User.class);
    assertThat(userFirst.getName()).isEqualTo(userName);
    assertThat(userFirst.getAge()).isEqualTo(userAge);

    // Act: Вызываем resolve для User с другими параметрами
    userName = "Alexei";
    userAge = 45;
    User userSecond = IoC.resolve(dependencyName, userName, userAge);
    assertThat(userSecond)
        .as("IoC should return a non-null User instance")
        .isNotNull()
        .isInstanceOf(User.class);
    assertThat(userSecond.getName()).isEqualTo(userName);
    assertThat(userSecond.getAge()).isEqualTo(userAge);

    // Ensure that two different instances are created
    assertThat(userFirst).isNotSameAs(userSecond);
  }

  @Test
  @DisplayName("Should throw exception when IoC try to resolve not exist dependency")
  void shouldThrowExceptionWhenResolveNotExistDependency() {
    // Arrange
    // Создаем хранилище для будущих стратегий
    ConcurrentHashMap<String, Function<Object[], Object>> scopesMap = new ConcurrentHashMap<>();
    // Обновляем базовую стратегию("update.ioc.resolve.dependency.strategy") IoC, чтобы она умела
    // искать в нашем scopesMap.
    UpdateIocResolveDependencyStrategyCommand updateCmd =
        IoC.resolve(
            "update.ioc.resolve.dependency.strategy",
            (Function<BiFunction<String, Object[], Object>, BiFunction<String, Object[], Object>>)
                oldStrategy ->
                    (dependency, args) -> {
                      if (scopesMap.containsKey(dependency)) {
                        Function<Object[], Object> strategy = scopesMap.get(dependency);
                        return strategy.apply(args);
                      }
                      return oldStrategy.apply(dependency, args);
                    });
    updateCmd.execute();
    // Регистрируем стратегию создания User
    String dependencyName = "ru.otus.main_patterns.hw05.model.User";
    scopesMap.put(
        dependencyName,
        args -> {
          try {
            return ConstructorUtils.invokeConstructor(Class.forName(dependencyName), args);
          } catch (Exception e) {
            throw new RuntimeException("Failed to create instance of " + dependencyName, e);
          }
        });

    // Act: Вызываем resolve для несуществующей зависимости "Person"
    String notExistDependency = "ru.otus.main_patterns.hw05.model.Person";
    assertThatThrownBy(
            () -> {
              IoC.resolve(notExistDependency, "Bob", 54);
            })
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Dependency " + notExistDependency + " is not found.");
  }
}
