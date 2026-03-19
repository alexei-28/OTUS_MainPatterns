package ru.otus.main_patterns.hw08.command;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MoveCommand implements Command {
  private final int initialVelocity;
  private static final Logger logger = LoggerFactory.getLogger(MoveCommand.class);

  public MoveCommand(Map<String, Object> args) {
    this.initialVelocity = (int) args.get("initialVelocity");
  }

  @Override
  public void execute() {
    logger.debug("execute, initialVelocity = {}", initialVelocity);
  }
}
