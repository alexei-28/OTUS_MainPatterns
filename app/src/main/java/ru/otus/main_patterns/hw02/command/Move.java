package ru.otus.main_patterns.hw02.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.main_patterns.hw02.inter.Movable;
import ru.otus.main_patterns.hw02.model.Point;
import ru.otus.main_patterns.hw02.model.Velocity;

/*-
  1. Решение#1 (неправильное) - нарушает SOLID принцип Open Closed Principe (OCP).

   public class Spaceship implements Movable {
       @Override
       public Point getLocation() {
           return null;
       }
       @Override
       public Velocity getVelocity() {
           return null;
       }
       @Override
       public void setLocation(Point location) {
       }
  }

  Проблема этого подхода состоит в том, что если изменятся бизнес требования,
  то придется изменить имплементацию интерфейса Movable, т.е изменить существующий код (класс Spaceship).
  Таким образом мы нарушаем OCP.

  2. Решение#2 (правильное) - не нарушает SOLID принцип OCP.
     Создаем класс Move, который содержит ссылку(не реализует) на интерфейс Movable.
     Таким образом при изменении бизнес требований (при изменении имплементации Movable), это не затронет класс Move.
     Класс Move останется неизменным.
*/
public class Move {
  private final Movable movable;

  private static final Logger logger = LoggerFactory.getLogger(Move.class);

  public Move(Movable movable) {
    this.movable = movable;
  }

  public void execute() {
    Point point = movable.getLocation();
    Velocity velocity = movable.getVelocity();
    Point pointEnd = point.plus(velocity);
    movable.setLocation(pointEnd);
  }
}
