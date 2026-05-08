package ru.otus.main_patterns.hw13.handler;

import ru.otus.main_patterns.hw13.IoC;
import ru.otus.main_patterns.hw13.UObject;
import ru.otus.main_patterns.hw13.command.Command;

public class StopMoveHandler implements ActionHandler {

  @Override
  public Command handle(UObject order, UObject gameObject) {
    return IoC.resolve("Commands.StopMove", gameObject);
  }
}
