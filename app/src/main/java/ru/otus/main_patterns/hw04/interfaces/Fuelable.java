package ru.otus.main_patterns.hw04.interfaces;

import ru.otus.main_patterns.hw04.Fuel;

public interface Fuelable {
  public Fuel getFuel();

  public Fuel getConsumedFuel();

  public void setFuel(Fuel fuel);
}
