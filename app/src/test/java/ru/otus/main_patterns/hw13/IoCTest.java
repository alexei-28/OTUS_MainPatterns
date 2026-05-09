package ru.otus.main_patterns.hw13;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class IoCTest {

  @BeforeEach
  void setUp() {
    IoC.clear();
  }

  @Test
  void shouldResolveFromGlobalContainer() {
    IoC.register("test.key", args -> "global-value");

    String result = IoC.resolve("test.key");

    assertThat(result).isEqualTo("global-value");
  }

  @Test
  void shouldPassArgumentsToFactory() {
    IoC.register("sum", args -> (int) args[0] + (int) args[1]);

    int result = IoC.resolve("sum", 2, 3);

    assertThat(result).isEqualTo(5);
  }

  @Test
  void shouldThrowWhenGlobalDependencyNotFound() {
    assertThatThrownBy(() -> IoC.resolve("missing"))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Dependency not found");
  }

  @Test
  void shouldResolveFromScopeWhenPresent() {
    IoC.register("player1", "service", args -> "scoped-value");

    String result = IoC.resolve("player1", "service");

    assertThat(result).isEqualTo("scoped-value");
  }

  @Test
  void shouldFallbackToGlobalWhenScopeMissingKey() {
    IoC.register("service", args -> "global-value");

    IoC.createScope("player1"); // empty scope

    String result = IoC.resolve("player1", "service");

    assertThat(result).isEqualTo("global-value");
  }

  @Test
  void shouldPreferScopeOverGlobal() {
    IoC.register("service", args -> "global");
    IoC.register("player1", "service", args -> "scoped");

    String result = IoC.resolve("player1", "service");

    assertThat(result).isEqualTo("scoped");
  }

  @Test
  void scopesShouldBeIsolatedBetweenPlayers() {
    // act
    IoC.register("player1", "service", args -> "p1");
    IoC.register("player2", "service", args -> "p2");

    // assert
    assertThat((String) IoC.resolve("player1", "service")).isEqualTo("p1");
    assertThat((String) IoC.resolve("player2", "service")).isEqualTo("p2");
  }

  @Test
  void clearShouldRemoveAllRegistrations() {
    // arrange
    IoC.register("test", args -> "value");
    IoC.createScope("player1");
    IoC.register("player1", "test", args -> "value2");

    // act
    IoC.clear();

    // assert
    assertThatThrownBy(() -> IoC.resolve("test")).isInstanceOf(RuntimeException.class);
  }

  @Test
  void factoryShouldBeExecutedOnResolve() {
    // act
    AtomicBoolean executed = new AtomicBoolean(false);
    IoC.register(
        "lazy",
        args -> {
          executed.set(true);
          return "ok";
        });

    // act
    IoC.resolve("lazy");

    // assert
    assertThat(executed.get()).isTrue();
  }

  @Test
  void shouldFallbackWhenScopeDoesNotExist() {
    // arrange
    IoC.register("global", args -> "global-value");

    // act
    String result = IoC.resolve("unknown-player", "global");

    // assert
    assertThat(result).isEqualTo("global-value");
  }
}
