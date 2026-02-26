package ru.otus.main_patterns.hw06.entity;

import java.util.Objects;

public class Velocity {
  private double dx;
  private double dy;

  public Velocity(double dx, double dy) {
    this.dx = dx;
    this.dy = dy;
  }

  public double getDx() {
    return dx;
  }

  public void setDx(double dx) {
    this.dx = dx;
  }

  public double getDy() {
    return dy;
  }

  public void setDy(double dy) {
    this.dy = dy;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    Velocity velocity = (Velocity) o;
    return Double.compare(dx, velocity.dx) == 0 && Double.compare(dy, velocity.dy) == 0;
  }

  @Override
  public int hashCode() {
    return Objects.hash(dx, dy);
  }

  @Override
  public String toString() {
    return "Velocity{" + "dx=" + dx + ", dy=" + dy + '}';
  }
}
