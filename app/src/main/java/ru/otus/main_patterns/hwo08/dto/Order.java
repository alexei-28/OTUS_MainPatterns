package ru.otus.main_patterns.hwo08.dto;

import java.util.Map;
import ru.otus.main_patterns.hwo08.command.Operation;

public class Order {
  private String id;
  private String gameId;
  private String playerId;
  private Operation operationId;
  private Map<String, Object> operationArgs;

  public Order() {}

  public String getGameId() {
    return gameId;
  }

  public void setGameId(String gameId) {
    this.gameId = gameId;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getPlayerId() {
    return playerId;
  }

  public void setPlayerId(String playerId) {
    this.playerId = playerId;
  }

  public Operation getOperationId() {
    return operationId;
  }

  public void setOperationId(Operation operationId) {
    this.operationId = operationId;
  }

  public Map<String, Object> getOperationArgs() {
    return operationArgs;
  }

  public void setOperationArgs(Map<String, Object> operationArgs) {
    this.operationArgs = operationArgs;
  }

  @Override
  public String toString() {
    return "\nOrder{"
        + "id='"
        + id
        + '\''
        + ", gameId='"
        + gameId
        + '\''
        + ", playerId='"
        + playerId
        + '\''
        + ", operationId="
        + operationId
        + ", operationArgs="
        + operationArgs
        + '}';
  }
}
