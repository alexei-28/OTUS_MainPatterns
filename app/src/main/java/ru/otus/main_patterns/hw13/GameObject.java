package ru.otus.main_patterns.hw13;

import java.util.HashMap;
import java.util.Map;

public class GameObject implements UObject {

  private final Map<String, Object> properties = new HashMap<>();

  @Override
  public Object getProperty(String key) {
    return properties.get(key);
  }

  @Override
  public void setProperty(String key, Object value) {
    properties.put(key, value);
  }
}
