package ru.otus.main_patterns.hw12;

import java.util.ArrayList;
import java.util.List;

public class Cell {
  private final List<GameObject> objects = new ArrayList<>();

  public List<GameObject> getObjects() {
    return objects;
  }
}
