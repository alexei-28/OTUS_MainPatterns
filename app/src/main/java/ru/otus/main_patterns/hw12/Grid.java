package ru.otus.main_patterns.hw12;

import java.util.*;

public class Grid {
  private final int cellSize;
  private final int offsetX;
  private final int offsetY;

  private final Map<String, Cell> cells = new HashMap<>();

  public Grid(int cellSize, int offsetX, int offsetY) {
    this.cellSize = cellSize;
    this.offsetX = offsetX;
    this.offsetY = offsetY;
  }

  private String key(int x, int y) {
    return x + "_" + y;
  }

  private int cellX(int x) {
    return (x + offsetX) / cellSize;
  }

  private int cellY(int y) {
    return (y + offsetY) / cellSize;
  }

  public Set<Cell> getCellsForObject(GameObject obj) {
    Set<Cell> result = new HashSet<>();

    int minX = cellX(obj.getX() - obj.getSize());
    int maxX = cellX(obj.getX() + obj.getSize());
    int minY = cellY(obj.getY() - obj.getSize());
    int maxY = cellY(obj.getY() + obj.getSize());

    for (int x = minX; x <= maxX; x++) {
      for (int y = minY; y <= maxY; y++) {
        result.add(getCell(x, y));
      }
    }
    return result;
  }

  private Cell getCell(int x, int y) {
    return cells.computeIfAbsent(key(x, y), k -> new Cell());
  }

  public Collection<Cell> getAllCells() {
    return cells.values();
  }
}
