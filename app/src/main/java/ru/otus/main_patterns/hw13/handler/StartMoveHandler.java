package ru.otus.main_patterns.hw13.handler;

import ru.otus.main_patterns.hw13.IoC;
import ru.otus.main_patterns.hw13.UObject;
import ru.otus.main_patterns.hw13.command.Command;

public class StartMoveHandler implements ActionHandler {

  @Override
  public Command handle(UObject order, UObject gameObject) {
    int velocity = (int) order.getProperty("initialVelocity");
    return IoC.resolve("Commands.StartMove", gameObject, velocity);
  }
}
