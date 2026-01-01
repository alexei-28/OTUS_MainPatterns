package ru.otus.main_patterns.hw02.adapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.main_patterns.hw02.inter.Movable;
import ru.otus.main_patterns.hw02.inter.UObject;
import ru.otus.main_patterns.hw02.model.Point;
import ru.otus.main_patterns.hw02.model.Velocity;

public class MoveAdapter implements Movable {
  public static final String POINT = "point";
  public static final String DIRECTION = "Direction";
  public static final String DIRECTIONS_NUMBER = "DirectionsNumber";
  public static final String VELOCITY = "velocity";
  private final UObject uObject;

  private static final Logger logger = LoggerFactory.getLogger(MoveAdapter.class);

  public MoveAdapter(UObject uObject) {
    this.uObject = uObject;
  }

  @Override
  public Point getLocation() {
    return (Point) uObject.getProperty(POINT);
  }

  @Override
  public void setLocation(Point point) {
    uObject.setProperty(POINT, point);
  }

  @Override
  public Velocity getVelocity() {
    return (Velocity) uObject.getProperty(VELOCITY);
  }

  /*-
  @Override
  public Velocity getVelocity() {
      int d = (int) uObject.getProperty(DIRECTION);
      int n = (int) uObject.getProperty(DIRECTIONS_NUMBER);
      Velocity velocity = (Velocity) uObject.getProperty(VELOCITY);
      return new Velocity(velocity.getDx() * Math.cos((double) d / 360 * n), velocity.getDy() * Math.sin(((double) d / 360 * n)));
  }
   */
}
