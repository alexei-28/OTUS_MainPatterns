package ru.otus.main_patterns.hw12.handler;

import ru.otus.main_patterns.hw12.GameObject;

public class DamageHandler extends Handler {

  @Override
  protected boolean process(GameObject a, GameObject b) {
    System.out.println("DamageHandler, Damage applied to " + a.getId() + " and " + b.getId());
    return true;
  }
}
