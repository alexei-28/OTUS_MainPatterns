package ru.otus.main_patterns.hw12.command;

import java.util.List;
import java.util.Set;
import ru.otus.main_patterns.hw12.Cell;
import ru.otus.main_patterns.hw12.GameObject;
import ru.otus.main_patterns.hw12.Grid;
import ru.otus.main_patterns.hw12.handler.CollisionCheckHandler;
import ru.otus.main_patterns.hw12.handler.DamageHandler;
import ru.otus.main_patterns.hw12.handler.Handler;
import ru.otus.main_patterns.hw12.interfaces.Command;

/*
   1. Удаляет объект из старых клеток
   2. Добавляет в новые клетки (из двух grid)
   3. Генерирует CollisionCommand в MacroCommand
*/
public class UpdateObjectCellCommand implements Command {
  private final GameObject gameObject;
  private final List<Grid> grids;
  private final MacroCommand macroCommand;

  public UpdateObjectCellCommand(
      GameObject gameObject, List<Grid> grids, MacroCommand macroCommand) {
    this.gameObject = gameObject;
    this.grids = grids;
    this.macroCommand = macroCommand;
  }

  @Override
  public void execute() {
    // 1. удалить из старых клеток
    for (Cell cell : gameObject.getCells()) {
      cell.getObjects().remove(gameObject);
    }
    gameObject.getCells().clear();

    // 2. добавить в новые клетки (из всех grid)
    for (Grid grid : grids) {
      Set<Cell> newCells = grid.getCellsForObject(gameObject);
      for (Cell cell : newCells) {
        // 3. создать команды коллизий
        for (GameObject other : cell.getObjects()) {
          // Chain of Responsibility
          Handler handler = buildChain(this.gameObject);
          macroCommand.add(new CollisionCommand(this.gameObject, other, handler));
        }
        cell.getObjects().add(gameObject);
        gameObject.getCells().add(cell);
      }
    }
  }

  private Handler buildChain(GameObject gameObjectA) {
    Handler handler = new CollisionCheckHandler();
    if (gameObjectA.getId() % 2 == 0) {
      handler.setNext(new DamageHandler());
    }
    return handler;
  }
}
