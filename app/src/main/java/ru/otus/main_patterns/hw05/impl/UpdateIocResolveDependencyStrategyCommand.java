package ru.otus.main_patterns.hw05.impl;

import java.util.function.BiFunction;
import java.util.function.Function;
import ru.otus.main_patterns.hw05.core.IoC;
import ru.otus.main_patterns.hw05.interfaces.Command;

public class UpdateIocResolveDependencyStrategyCommand implements Command {
  private Function<BiFunction<String, Object[], Object>, BiFunction<String, Object[], Object>>
      updateIoCStrategy;

  public UpdateIocResolveDependencyStrategyCommand(
      Function<BiFunction<String, Object[], Object>, BiFunction<String, Object[], Object>>
          updateIoCStrategy) {
    this.updateIoCStrategy = updateIoCStrategy;
  }

  @Override
  public void execute() {
    IoC.strategy = updateIoCStrategy.apply(IoC.strategy);
  }
}
