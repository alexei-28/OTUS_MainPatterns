package ru.otus.main_patterns.hw06.interfaces;

import ru.otus.main_patterns.hw06.codegen.annotations.GenerateImpl;
import ru.otus.main_patterns.hw06.entity.Vector;

@GenerateImpl
public interface Movable {

  Vector getPosition();

  void setPosition(Vector newValue);

  Vector getVelocity();

  void finish();
}
