package ru.otus.main_patterns.hw08.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.util.List;
import java.util.Map;

public class GameConfig {
  private Map<String, Game> games;

  public Map<String, Game> getGames() {
    return games;
  }

  public void setGames(Map<String, Game> games) {
    this.games = games;
  }

  public static class Game {
    private Map<String, Player> players;

    public Map<String, Player> getPlayers() {
      return players;
    }

    public void setPlayers(Map<String, Player> players) {
      this.players = players;
    }
  }

  public static class Player {
    private List<String> operations;

    public List<String> getOperations() {
      return operations;
    }

    public void setOperations(List<String> operations) {
      this.operations = operations;
    }
  }

  @Override
  public String toString() {
    try {
      ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
      return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(this);
    } catch (Exception e) {
      return super.toString();
    }
  }
}
