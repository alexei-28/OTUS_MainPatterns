package ru.otus.main_patterns.hw11.context;

import ru.otus.main_patterns.hw11.interfaces.CommandState;

public class Context {
  private CommandState commandState;

  public Context(CommandState commandState) {
    this.commandState = commandState;
  }

  public CommandState getState() {
    return commandState;
  }

  public void setState(CommandState commandState) {
    this.commandState = commandState;
  }
}
