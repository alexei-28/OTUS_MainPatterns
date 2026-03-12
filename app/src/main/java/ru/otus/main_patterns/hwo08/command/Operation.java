package ru.otus.main_patterns.hwo08.command;

import java.util.Map;
import java.util.function.Function;

public enum Operation {
  MOVE_STRAIGHT("move.straight", MoveCommand::new),
  ROTATE("rotate", RotateCommand::new),
  BURN_FUEL("burn.fuel", BurnFuelCommand::new),
  SHOOT("shoot", ShootCommand::new),
  TELEPORT("teleport", TeleportCommand::new);

  private final Function<Map<String, Object>, Command> factory;
  private final String fullName;

  Operation(String fullName, Function<Map<String, Object>, Command> factory) {
    this.fullName = fullName;
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
