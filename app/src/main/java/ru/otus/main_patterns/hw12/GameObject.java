package ru.otus.main_patterns.hw12;

import java.util.HashSet;
import java.util.Set;

public class GameObject {
  private int id;
  private int x, y;
  private int size;

  private Set<Cell> cells = new HashSet<>();

  public GameObject(int id, int x, int y, int size) {
    this.id = id;
    this.x = x;
    this.y = y;
    this.size = size;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public int getX() {
    return x;
  }

  public void setX(int x) {
    this.x = x;
  }

  public int getY() {
    return y;
  }

  public void setY(int y) {
    this.y = y;
  }

  public int getSize() {
    return size;
  }

  public void setSize(int size) {
    this.size = size;
  }

  public Set<Cell> getCells() {
    return cells;
  }

  public void setCells(Set<Cell> cells) {
    this.cells = cells;
  }
}
