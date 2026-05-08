package ru.otus.main_patterns.hw13;

import java.util.HashMap;
import java.util.Map;
import ru.otus.main_patterns.hw13.handler.ActionHandler;

public class ActionRegistry {

  private static final Map<String, ActionHandler> handlers = new HashMap<>();

  public static void register(String action, ActionHandler handler) {
    handlers.put(action, handler);
  }

  public static ActionHandler get(String action) {
    ActionHandler handler = handlers.get(action);

    if (handler == null) {
      throw new RuntimeException("Unknown action: " + action);
    }
    return handler;
  }

  public static void clear() {
    handlers.clear();
  }
}
