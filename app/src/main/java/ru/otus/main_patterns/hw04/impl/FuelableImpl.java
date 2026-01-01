package ru.otus.main_patterns.hw04.impl;

import ru.otus.main_patterns.hw04.Fuel;
import ru.otus.main_patterns.hw04.interfaces.Fuelable;

public class FuelableImpl implements Fuelable {
  private Fuel fuel;
  private Fuel consumedFuel;

  public FuelableImpl(Fuel fuel, Fuel consumedFuel) {
    this.fuel = fuel;
    this.consumedFuel = consumedFuel;
  }

  @Override
  public Fuel getFuel() {
    return fuel;
  }

  @Override
  public Fuel getConsumedFuel() {
    return consumedFuel;
  }

  @Override
  public void setFuel(Fuel fuel) {
    this.fuel = fuel;
  }
}
