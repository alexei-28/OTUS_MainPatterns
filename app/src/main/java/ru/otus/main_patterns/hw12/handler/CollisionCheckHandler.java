package ru.otus.main_patterns.hw12.handler;

import ru.otus.main_patterns.hw12.GameObject;

public class CollisionCheckHandler extends Handler {

  @Override
  protected boolean process(GameObject a, GameObject b) {
    int dx = a.getX() - b.getX();
    int dy = a.getY() - b.getY();
    int r = a.getSize() + b.getSize();
    if (dx * dx + dy * dy <= r * r) {
      System.out.println(
          "\nCollisionCheckHandler, Collision detected: " + a.getId() + " & " + b.getId());
      return true;
    }
    return false; // остановка цепочки
  }
}
