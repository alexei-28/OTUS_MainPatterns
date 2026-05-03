package ru.otus.main_patterns.hw12.command;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.*;
import org.junit.jupiter.api.Test;
import ru.otus.main_patterns.hw12.Cell;
import ru.otus.main_patterns.hw12.GameObject;
import ru.otus.main_patterns.hw12.Grid;
import ru.otus.main_patterns.hw12.handler.CollisionCheckHandler;
import ru.otus.main_patterns.hw12.handler.DamageHandler;

class UpdateObjectCellCommandTest {

  // Базовый тест: объект попадает в клетки
  @Test
  void shouldAddObjectToCells() {
    // Arrange
    Grid grid = new Grid(50, 0, 0);
    List<Grid> grids = Arrays.asList(grid);
    MacroCommand macro = new MacroCommand();
    GameObject obj = new GameObject(1, 10, 10, 5);

    // Act
    new UpdateObjectCellCommand(obj, grids, macro).execute();

    // Assert
    assertFalse(obj.getCells().isEmpty(), "Object should be assigned to cells");
  }

  // Объект удаляется из старых клеток
  @Test
  void shouldRemoveObjectFromOldCells() {
    // Arrange
    Grid grid = new Grid(50, 0, 0);
    List<Grid> grids = Arrays.asList(grid);
    MacroCommand macro = new MacroCommand();
    GameObject obj = new GameObject(1, 10, 10, 5);
    // первый апдейт
    new UpdateObjectCellCommand(obj, grids, macro).execute();
    Set<Cell> oldCells = new HashSet<>(obj.getCells());
    // двигаем объект
    obj.setX(200);
    obj.setY(200);

    // Act
    new UpdateObjectCellCommand(obj, grids, macro).execute();

    // Assert
    for (Cell c : oldCells) {
      assertFalse(c.getObjects().contains(obj), "Old cell should not contain object");
    }
  }

  //  Объект добавляется в новые клетки
  @Test
  void shouldAddObjectToNewCellsAfterMove() {
    // Arrange
    Grid grid = new Grid(50, 0, 0);
    List<Grid> grids = Arrays.asList(grid);
    MacroCommand macro = new MacroCommand();
    GameObject obj = new GameObject(1, 10, 10, 5);
    // Act
    new UpdateObjectCellCommand(obj, grids, macro).execute();
    obj.setX(200);
    obj.setY(200);
    new UpdateObjectCellCommand(obj, grids, macro).execute();
    boolean found = false;
    for (Cell c : obj.getCells()) {
      if (c.getObjects().contains(obj)) {
        found = true;
      }
    }

    // Assert
    assertTrue(found, "Object should be in new cells");
  }

  //  Генерация collision-команд
  @Test
  void shouldCreateCollisionCommands() {
    // Arrange
    Grid grid = new Grid(50, 0, 0);
    List<Grid> grids = Arrays.asList(grid);
    MacroCommand macro = new MacroCommand();
    GameObject obj1 = new GameObject(1, 10, 10, 5);
    GameObject obj2 = new GameObject(2, 12, 12, 5);

    // Act
    new UpdateObjectCellCommand(obj1, grids, macro).execute();
    new UpdateObjectCellCommand(obj2, grids, macro).execute();

    // Assert
    assertFalse(macro.getCommands().isEmpty(), "Collision commands should be created");
  }

  // Объект может быть в нескольких клетках
  @Test
  void shouldOccupyMultipleCellsIfLarge() {
    // Arrange
    Grid grid = new Grid(50, 0, 0);
    List<Grid> grids = Arrays.asList(grid);
    MacroCommand macro = new MacroCommand();
    GameObject obj = new GameObject(1, 50, 50, 40); // большой радиус

    // Act
    new UpdateObjectCellCommand(obj, grids, macro).execute();

    // Assert
    assertTrue(obj.getCells().size() > 1, "Object should occupy multiple cells");
  }

  // Две сетки (offset grid)
  @Test
  void shouldWorkWithTwoGrids() {
    // Arrange
    Grid grid1 = new Grid(50, 0, 0);
    Grid grid2 = new Grid(50, 25, 25);
    List<Grid> grids = Arrays.asList(grid1, grid2);
    MacroCommand macro = new MacroCommand();
    GameObject obj = new GameObject(1, 49, 49, 5);

    // Act
    new UpdateObjectCellCommand(obj, grids, macro).execute();

    // Assert
    assertTrue(obj.getCells().size() >= 2, "Object should be in cells from both grids");
  }

  // Проверка Chain of Responsibility
  @Test
  void shouldStopChainIfNoCollision() {
    // Arrange
    CollisionCheckHandler h1 = new CollisionCheckHandler();
    DamageHandler h2 = spy(new DamageHandler());
    h1.setNext(h2);

    GameObject a = new GameObject(1, 0, 0, 5);
    GameObject b = new GameObject(2, 1000, 1000, 5);

    // если chain работает правильно — DamageHandler НЕ вызовется
    // Act
    h1.handle(a, b);

    // Assert
    verify(h2, never()).handle(any(), any());
  }
}
