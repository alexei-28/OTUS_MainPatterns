package ru.otus.main_patterns.hw05.commands;

import java.util.Map;
import java.util.function.Function;
import ru.otus.main_patterns.hw05.core.IoC;
import ru.otus.main_patterns.hw05.interfaces.Command;

/** Регистрация зависимости. Обычно команда регистрируется сразу после ее создания. */
public class RegisterDependencyCommand implements Command {
  private final String dependency;
  private final Function<Object[], Object> dependencyResolverStrategy;

  public RegisterDependencyCommand(
      String dependency, Function<Object[], Object> dependencyResolverStrategy) {
    this.dependency = dependency;
    this.dependencyResolverStrategy = dependencyResolverStrategy;
  }

  @Override
  public void execute() {
    // Из текущего ThreadLocal получаем ссылку на текущий контекст.
    Map<String, Function<Object[], Object>> currentScope = IoC.resolve("ioc.scope.current");
    // В текущем контексте добавляем(регистрируем) зависимость.
    currentScope.put(dependency, dependencyResolverStrategy);
  }
}
