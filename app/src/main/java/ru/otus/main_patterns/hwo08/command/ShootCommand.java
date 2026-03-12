package ru.otus.main_patterns.hwo08.command;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShootCommand implements Command {
  private final Weapon weaponId;
  private final Direction direction;

  private static final Logger logger = LoggerFactory.getLogger(ShootCommand.class);

  public ShootCommand(Map<String, Object> args) {
    this.weaponId = Weapon.valueOf(((String) args.get("weaponId")).toUpperCase());
    this.direction = Direction.valueOf(((String) args.get("direction")).toUpperCase());
  }

  @Override
  public void execute() {
    logger.debug("execute, weaponId = {}, direction = {}", weaponId, direction);
  }
}
