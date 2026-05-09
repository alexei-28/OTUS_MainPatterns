package ru.otus.main_patterns.hw13.handler;

import ru.otus.main_patterns.hw13.IoC;
import ru.otus.main_patterns.hw13.UObject;
import ru.otus.main_patterns.hw13.command.Command;

public class ShootHandler implements ActionHandler {

  @Override
  public Command handle(UObject order, UObject gameObject) {
    String weaponId = (String) order.getProperty("weaponId");
    String direction = (String) order.getProperty("direction");
    return IoC.resolve("Commands.Shoot", gameObject, weaponId, direction);
  }
}
