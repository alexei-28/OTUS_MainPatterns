package ru.otus.main_patterns.hw05.commands;

import java.util.Map;
import java.util.function.Function;
import ru.otus.main_patterns.hw05.core.IoC;
import ru.otus.main_patterns.hw05.interfaces.Command;

/** Команда для регистрации зависимости. Обычно команда регистрируется сразу после ее создания. */
public class RegisterDependencyCommand implements Command {
  private final String dependencyName;
  private final Function<Object[], Object> dependencyResolverStrategy;

  /**
   * @param dependencyName имя зависимости
   * @param dependencyResolverStrategy лямбда-выражение, которое будет вызываться
   */
  public RegisterDependencyCommand(
      String dependencyName, Function<Object[], Object> dependencyResolverStrategy) {
    this.dependencyName = dependencyName;
    this.dependencyResolverStrategy = dependencyResolverStrategy;
  }

  @Override
  public void execute() {
    // Из текущего ThreadLocal получаем ссылку на текущий контекст(scope).
    Map<String, Function<Object[], Object>> currentScope = IoC.resolve("ioc.scope.current");
    // В текущем контексте добавляем(регистрируем) зависимость.
    currentScope.put(dependencyName, dependencyResolverStrategy);
  }

  public String getDependencyName() {
    return dependencyName;
  }

  public Function<Object[], Object> getDependencyResolverStrategy() {
    return dependencyResolverStrategy;
  }
}
