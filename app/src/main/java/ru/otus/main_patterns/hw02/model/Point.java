package ru.otus.main_patterns.hw02.model;

import java.util.Objects;

public class Point {
  private final double x;
  private final double y;

  public Point(double x, double y) {
    this.x = x;
    this.y = y;
  }

  /*-
     Прямолинейное движение объекта (параллельный перенос).
      Положение точки#1(Location) = (x,y)
      Вектор скорости(Velocity) = (dx, dy)
      Положение точки#2 = (x+dx, y+dy)
  */
  public Point plus(Velocity velocity) {
    return new Point(this.x + velocity.getDx(), this.y + velocity.getDy());
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    Point point = (Point) o;
    return x == point.x && y == point.y;
  }

  @Override
  public int hashCode() {
    return Objects.hash(x, y);
  }

  @Override
  public String toString() {
    return "Point{" + "x=" + x + ", y=" + y + '}';
  }
}
