package ru.otus.main_patterns.hwo08.command;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TeleportCommand implements Command {
  private final int x;
  private final int y;

  private static final Logger logger = LoggerFactory.getLogger(TeleportCommand.class);

  public TeleportCommand(Map<String, Object> args) {
    this.x = (int) args.get("x");
    this.y = (int) args.get("y");
  }

  @Override
  public void execute() {
    logger.debug("\nTeleportCommand, execute, x: {}, y: {}", x, y);
  }
}
