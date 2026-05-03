package ru.otus.main_patterns.hw12.handler;

import ru.otus.main_patterns.hw12.GameObject;

public abstract class Handler {
  private Handler next;

  public void setNext(Handler next) {
    this.next = next;
  }

  public void handle(GameObject gameObjectA, GameObject gameObjectB) {
    if (process(gameObjectA, gameObjectB) && next != null) {
      next.handle(gameObjectA, gameObjectB);
    }
  }

  protected abstract boolean process(GameObject a, GameObject b);
}
