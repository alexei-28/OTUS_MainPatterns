package ru.otus.main_patterns.hw02.inter;

import ru.otus.main_patterns.hw02.model.Direction;

public interface Rotatable {
  Direction getDirection();

  int getAngularVelocity();

  void setDirection(Direction newV);
}
