package ru.otus.main_patterns.hw07.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.main_patterns.hw07.interfaces.Command;

public class RotateCommand implements Command {
  private static final Logger logger = LoggerFactory.getLogger(RotateCommand.class);

  @Override
  public void execute() {
    logger.debug("\nRotateCommand, execute");
  }
}
