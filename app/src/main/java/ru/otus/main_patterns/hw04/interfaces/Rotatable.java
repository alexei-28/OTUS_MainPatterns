package ru.otus.main_patterns.hw04.interfaces;

import ru.otus.main_patterns.hw04.model.Direction;

public interface Rotatable {
  Direction getDirection();

  int getAngularVelocity();

  void setDirection(Direction newV);
}
