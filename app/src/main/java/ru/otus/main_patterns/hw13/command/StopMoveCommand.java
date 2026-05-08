package ru.otus.main_patterns.hw13.command;

import ru.otus.main_patterns.hw13.UObject;

public class StopMoveCommand implements Command {
  private final UObject ship;

  public StopMoveCommand(UObject ship) {
    this.ship = ship;
  }

  @Override
  public void execute() {
    ship.setProperty("velocity", 0);
    ship.setProperty("moving", false);
  }
}
