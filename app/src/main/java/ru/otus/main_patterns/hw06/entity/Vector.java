package ru.otus.main_patterns.hw06.entity;

import java.util.Objects;
import ru.otus.main_patterns.hw02.model.Velocity;

public class Vector {
  private final double x;
  private final double y;

  public Vector(double x, double y) {
    this.x = x;
    this.y = y;
  }

  public Vector plus(Velocity velocity) {
    return new Vector(this.x + velocity.getDx(), this.y + velocity.getDy());
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    Vector vector = (Vector) o;
    return Double.compare(x, vector.x) == 0 && Double.compare(y, vector.y) == 0;
  }

  @Override
  public int hashCode() {
    return Objects.hash(x, y);
  }

  @Override
  public String toString() {
    return "Vector{" + "x=" + x + ", y=" + y + '}';
  }
}
