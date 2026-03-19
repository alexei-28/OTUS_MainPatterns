package ru.otus.main_patterns.hw08.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.InputStream;
import ru.otus.main_patterns.hw08.command.InterpretCommand;
import ru.otus.main_patterns.hw08.config.GameConfig;

public class GameConfigService {
  private static final GameConfig GAME_CONFIG;

  static {
    try {
      ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
      InputStream is =
          InterpretCommand.class.getClassLoader().getResourceAsStream("game-config.yml");
      if (is == null) {
        throw new RuntimeException("game-config.yml not found in resources");
      }
      GAME_CONFIG = mapper.readValue(is, GameConfig.class);
    } catch (Exception e) {
      throw new RuntimeException("Failed to load game-config.yml", e);
    }
  }

  public static GameConfig get() {
    return GAME_CONFIG;
  }
}
