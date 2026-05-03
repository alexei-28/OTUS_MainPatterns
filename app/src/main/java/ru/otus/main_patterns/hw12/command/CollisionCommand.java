package ru.otus.main_patterns.hw12.command;

import ru.otus.main_patterns.hw12.GameObject;
import ru.otus.main_patterns.hw12.handler.Handler;
import ru.otus.main_patterns.hw12.interfaces.Command;

public class CollisionCommand implements Command {
  private final GameObject gameObjectA;
  private final GameObject gameObjectB;
  private final Handler handler;

  public CollisionCommand(GameObject gameObjectA, GameObject gameObjectB, Handler handler) {
    this.gameObjectA = gameObjectA;
    this.gameObjectB = gameObjectB;
    this.handler = handler;
  }

  @Override
  public void execute() {
    handler.handle(gameObjectA, gameObjectB);
  }
}
