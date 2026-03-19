package ru.otus.main_patterns.hw08.command;

import java.util.Map;
import java.util.function.Function;

public enum Operation {
  MOVE_STRAIGHT(MoveCommand::new),
  ROTATE(RotateCommand::new),
  BURN_FUEL(BurnFuelCommand::new),
  SHOOT(ShootCommand::new),
  TELEPORT(TeleportCommand::new);

  private final Function<Map<String, Object>, Command> factory;

  Operation(Function<Map<String, Object>, Command> factory) {
    this.factory = factory;
  }

  public Command createCommand(Map<String, Object> operationArgs) {
    return factory.apply(operationArgs);
  }

  public static Operation getOperationByName(String operationId) {
    for (Operation op : values()) {
      if (op.name().equals(operationId)) {
        return op;
      }
    }
    // Если такой операции нет в природе — возвращаем "виртуальную" заглушку
    // Или кидаем исключение сразу здесь
    throw new UnsupportedOperationException("Unknown operation ID: " + operationId);
  }
}
