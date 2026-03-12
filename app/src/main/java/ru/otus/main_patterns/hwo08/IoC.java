package ru.otus.main_patterns.hwo08;

import java.util.function.BiFunction;
import java.util.function.Function;
import ru.otus.main_patterns.hwo08.command.UpdateIocResolveDependencyStrategyCommand;

public class IoC {

  public static <T> T resolve(String dependency, Object... args) {
    return (T) strategy.apply(dependency, args);
  }

  /**
   * Дефолтная стратегия, которая умеет заменять сама себя. При переопределении стратегии (через
   * {@code "update.ioc.resolve.dependency.strategy"}) ожидается лямбда-функция типа:
   *
   * <pre>{@code
   * Function<BiFunction<String, Object[], Object>, BiFunction<String, Object[], Object>>
   * }</pre>
   *
   * <p>Она принимает текущую стратегию и возвращает новую.
   *
   * <p><b>Примеры использования:</b>
   *
   * <pre>{@code
   * InterfaceA valA = IoC.<InterfaceA>resolve("productA", 1, "abc");
   * InterfaceB valB = IoC.<InterfaceB>resolve("productB", "1234567");
   * InterfaceC valC = IoC.<InterfaceC>resolve("productC");
   * Command command = IoC.<Command>resolve("Движение по прямой");
   *
   * }</pre>
   */
  @SuppressWarnings("unchecked")
  public static BiFunction<String, Object[], Object> strategy =
      new BiFunction<String, Object[], Object>() {
        @Override
        public Object apply(String dependency, Object[] args) {
          if ("update.ioc.resolve.dependency.strategy".equals(dependency)) {
            Function<BiFunction<String, Object[], Object>, BiFunction<String, Object[], Object>>
                updateIoCStrategy =
                    (Function<
                            BiFunction<String, Object[], Object>,
                            BiFunction<String, Object[], Object>>)
                        args[0];
            return new UpdateIocResolveDependencyStrategyCommand(updateIoCStrategy);
          } else {
            throw new IllegalArgumentException(
                String.format("Dependency %s is not found.", dependency));
          }
        }
      };
}
