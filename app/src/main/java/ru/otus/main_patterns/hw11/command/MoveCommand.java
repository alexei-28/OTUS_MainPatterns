package ru.otus.main_patterns.hw11.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.main_patterns.hw11.interfaces.Command;

public class MoveCommand implements Command {
  private static final Logger logger = LoggerFactory.getLogger(MoveCommand.class);

  @Override
  public void execute() {
    logger.debug("\nMoveCommand, execute");
  }
}
