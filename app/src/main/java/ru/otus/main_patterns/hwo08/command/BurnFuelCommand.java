package ru.otus.main_patterns.hwo08.command;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BurnFuelCommand implements Command {
  private final int fuelAmount;
  private static final Logger logger = LoggerFactory.getLogger(BurnFuelCommand.class);

  public BurnFuelCommand(Map<String, Object> args) {
    this.fuelAmount = (int) args.get("fuelAmount");
  }

  @Override
  public void execute() {
    logger.debug("\nBurnFuelCommand, execute, fuelAmount = {}", fuelAmount);
  }
}
