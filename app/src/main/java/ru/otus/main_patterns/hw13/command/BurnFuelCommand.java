package ru.otus.main_patterns.hw13.command;

import ru.otus.main_patterns.hw13.UObject;

public class BurnFuelCommand implements Command {

  private final UObject ship;
  private final int fuelAmount;

  public BurnFuelCommand(UObject ship, int fuelAmount) {
    this.ship = ship;
    this.fuelAmount = fuelAmount;
  }

  @Override
  public void execute() {
    int fuel = (int) ship.getProperty("fuel");
    if (fuel < fuelAmount) {
      throw new RuntimeException("Not enough fuel");
    }
    ship.setProperty("fuel", fuel - fuelAmount);
  }
}
