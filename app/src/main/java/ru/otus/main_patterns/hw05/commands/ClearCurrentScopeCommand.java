package ru.otus.main_patterns.hw05.commands;

import ru.otus.main_patterns.hw05.interfaces.Command;

public class ClearCurrentScopeCommand implements Command {

  @Override
  public void execute() {
    InitCommand.currentScopeThreadLocal.remove();
  }
}
