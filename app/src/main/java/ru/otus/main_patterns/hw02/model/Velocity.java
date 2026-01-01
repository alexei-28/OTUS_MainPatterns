package ru.otus.main_patterns.hw02.model;

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
  public String toString() {
    return "Velocity{" + "dx=" + dx + ", dy=" + dy + '}';
  }
}
