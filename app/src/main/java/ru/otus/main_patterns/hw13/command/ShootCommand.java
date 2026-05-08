package ru.otus.main_patterns.hw13.command;

import ru.otus.main_patterns.hw13.UObject;

public class ShootCommand implements Command {
  private final UObject ship;
  private final String weaponId;
  private final String direction;

  public ShootCommand(UObject ship, String weaponId, String direction) {
    this.ship = ship;
    this.weaponId = weaponId;
    this.direction = direction;
  }

  @Override
  public void execute() {
    ship.setProperty("weaponId", weaponId);
    ship.setProperty("direction", direction);
  }
}
