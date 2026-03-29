package ru.otus.main_patterns.hw10.service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import ru.otus.main_patterns.hw10.command.*;
import ru.otus.main_patterns.hw10.dto.Order;

public class ScopeService {
  private static final ConcurrentHashMap<String, Function<Object[], Object>> scopesMap =
      new ConcurrentHashMap<>();

  private ScopeService() {}

  static {
    // Регистрируем стратегию создания команды Command
    scopesMap.putIfAbsent(
        "create.command",
        (Object[] args) -> {
          Order order = (Order) args[0];
          Operation operation = order.getOperationId();
          return operation.createCommand(order.getOperationArgs());
        });

    // Регистрируем стратегию создания команды QueueCommand
    scopesMap.putIfAbsent(
        "queue.command",
        (Object[] args) -> {
          Command command = (Command) args[0];
          return new QueueCommand(command);
        });
  }

  public static ConcurrentHashMap<String, Function<Object[], Object>> getGlobalScope() {
    return scopesMap;
  }
}
