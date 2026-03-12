package ru.otus.main_patterns.hwo08.command;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RotateCommand implements Command {
  private final int radius;
  private final int angularVelocity;

  private static final Logger logger = LoggerFactory.getLogger(RotateCommand.class);

  public RotateCommand(Map<String, Object> args) {
    this.radius = (int) args.get("radius");
    this.angularVelocity = (int) args.get("angularVelocity");
  }

  @Override
  public void execute() {
    logger.debug("execute, radius = {}, angularVelocity = {}", radius, angularVelocity);
  }
}
