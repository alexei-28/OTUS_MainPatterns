package ru.otus.main_patterns.hw13.command;

import ru.otus.main_patterns.hw13.UObject;

public class StartMoveCommand implements Command {
  private final UObject ship;
  private final int velocity;

  public StartMoveCommand(UObject ship, int velocity) {
    this.ship = ship;
    this.velocity = velocity;
  }

  @Override
  public void execute() {
    ship.setProperty("velocity", velocity);
    ship.setProperty("moving", true);
  }
}
