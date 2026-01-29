package ru.otus.main_patterns.hw05.commands;

import java.util.function.BiFunction;
import java.util.function.Function;
import ru.otus.main_patterns.hw05.core.IoC;
import ru.otus.main_patterns.hw05.interfaces.Command;

/**
 * Команда обновляет стратегию разрешения зависимостей IoC контейнера.
 *
 * <p>"<a
 * href="https://github.com/etyumentcev/appserver/blob/main/appserver/core/impl/UpdateIocResolveDependencyStrategyCommand.cs">Help</a>>
 */
public class UpdateIocResolveDependencyStrategyCommand implements Command {
  private final Function<BiFunction<String, Object[], Object>, BiFunction<String, Object[], Object>>
      updateIoCStrategy;

  public UpdateIocResolveDependencyStrategyCommand(
      Function<BiFunction<String, Object[], Object>, BiFunction<String, Object[], Object>>
          updater) {
    this.updateIoCStrategy = updater;
  }

  @Override
  public void execute() {
    // Accessing the static field from the Ioc class and applying the updater
    BiFunction<String, Object[], Object> updatedStrategy = updateIoCStrategy.apply(IoC.strategy);
    IoC.strategy = updatedStrategy;
  }
}
