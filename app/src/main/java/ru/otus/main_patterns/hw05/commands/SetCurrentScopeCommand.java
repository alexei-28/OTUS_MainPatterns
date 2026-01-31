package ru.otus.main_patterns.hw05.commands;

import ru.otus.main_patterns.hw05.interfaces.Command;

/**
 * Команда для установки текущей области видимости (scope) в текущем потоке. Использует ThreadLocal
 * для обеспечения изоляции контекстов между потоками.
 */
public class SetCurrentScopeCommand implements Command {
  private final Object scope;

  /** @param scope Объект, представляющий новую область видимости (обычно Map). */
  public SetCurrentScopeCommand(Object scope) {
    this.scope = scope;
  }

  /** Устанавливает переданный scope в ThreadLocal текущего потока. */
  @Override
  public void execute() {
    // Предполагается, что currentScopeThreadLocal — это статический ThreadLocal в классе
    // InitCommand
    InitCommand.currentScopeThreadLocal.set(scope);
  }
}
