package ru.otus.main_patterns.hw05.commands;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.function.BiFunction;
import java.util.function.Function;
import org.junit.jupiter.api.*;
import ru.otus.main_patterns.hw05.core.IoC;

class UpdateIocResolveDependencyStrategyCommandTest {
  private BiFunction<String, Object[], Object> originalStrategy;

  @BeforeEach
  void saveState() {
    // Save the real default strategy to restore after each test
    originalStrategy = IoC.strategy; // default dependency "update.ioc.resolve.dependency.strategy"

    IoC.strategy =
        new BiFunction<String, Object[], Object>() {
          @Override
          public Object apply(String dependency, Object[] objects) {
            return "base_result";
          }
        };
  }

  @AfterEach
  void restoreState() {
    IoC.strategy = originalStrategy;
  }

  @Test
  @DisplayName("Should update IoC strategy by wrapping the existing one")
  void shouldUpdateIocStrategy() {
    // Arrange: Создаем апдейтер, который добавляет префикс к результату старой стратегии
    Function<BiFunction<String, Object[], Object>, BiFunction<String, Object[], Object>> updater =
        oldStrategy ->
            new BiFunction<String, Object[], Object>() {
              @Override
              public Object apply(String dependency, Object[] args) {
                Object result = oldStrategy.apply(dependency, args);
                return "updated_" + result;
              }
            };
    UpdateIocResolveDependencyStrategyCommand command =
        new UpdateIocResolveDependencyStrategyCommand(updater);

    // Act
    command.execute();

    // Assert: Проверяем, что теперь стратегия возвращает модифицированный результат
    Object finalResult = IoC.strategy.apply("any.dependency", new Object[] {});
    assertThat(finalResult)
        .as("The strategy should be decorated with the 'updated_' prefix")
        .isEqualTo("updated_base_result");
  }

  @Test
  @DisplayName("Should support multiple updates (nesting strategies)")
  void shouldSupportNestedUpdates() {
    // Arrange: Первый апдейтер
    Function<BiFunction<String, Object[], Object>, BiFunction<String, Object[], Object>>
        firstUpdate =
            oldStrategy -> (dependency, args) -> "first_" + oldStrategy.apply(dependency, args);
    // Второй апдейтер
    Function<BiFunction<String, Object[], Object>, BiFunction<String, Object[], Object>>
        secondUpdate =
            oldStrategy -> (dependency, args) -> "second_" + oldStrategy.apply(dependency, args);

    // Act
    new UpdateIocResolveDependencyStrategyCommand(firstUpdate).execute();
    new UpdateIocResolveDependencyStrategyCommand(secondUpdate).execute();

    // Assert
    Object result = IoC.strategy.apply("dependency", new Object[] {});
    assertThat(result)
        .as("Strategies should be nested like pattern Decorator")
        .isEqualTo("second_first_base_result");
  }
}
