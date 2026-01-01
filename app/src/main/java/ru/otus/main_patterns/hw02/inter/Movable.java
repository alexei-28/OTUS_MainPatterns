package ru.otus.main_patterns.hw02.inter;

import ru.otus.main_patterns.hw02.model.Point;
import ru.otus.main_patterns.hw02.model.Velocity;

public interface Movable {
  Point getLocation();

  void setLocation(Point point);

  Velocity getVelocity();
}
