package ru.otus.main_patterns.hw06;

import java.util.function.BiFunction;
import ru.otus.main_patterns.hw06.generated.impl.MovableImpl;
import ru.otus.main_patterns.hw06.interfaces.UObject;

public class IoC {

  public static BiFunction<String, Object[], Object> strategy =
      (dependency, args) -> {
        if ("Adapter".equals(dependency)) {
          return new MovableImpl((UObject) args[1]);
        } else {
          throw new IllegalArgumentException(
              String.format("Dependency %s is not found.", dependency));
        }
      };

  public static <T> T resolve(String dependency, Object... args) {
    return (T) strategy.apply(dependency, args);
  }
}
