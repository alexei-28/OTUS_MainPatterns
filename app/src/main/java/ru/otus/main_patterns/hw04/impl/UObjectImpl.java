package ru.otus.main_patterns.hw04.impl;

import java.util.HashMap;
import java.util.Map;
import ru.otus.main_patterns.hw04.interfaces.UObject;

public class UObjectImpl implements UObject {
  private final Map<String, Object> map = new HashMap<>();

  @Override
  public void setProperty(String propertyName, Object value) {
    if (propertyName == null) {
      throw new IllegalArgumentException();
    } else {
      map.put(propertyName, value);
    }
  }

  @Override
  public Object getProperty(String propertyName) {
    return map.get(propertyName);
  }

  @Override
  public String toString() {
    return "UObjectImpl{" + "map=" + map + '}';
  }
}
