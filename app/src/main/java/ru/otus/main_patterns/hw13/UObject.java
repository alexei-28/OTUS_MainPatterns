package ru.otus.main_patterns.hw13;

/*
  Универсальный контейнер данных, который:
    - хранит приказ
    - хранит игровые объекты
    - хранит параметры команд

   Делает систему динамической.
*/
public interface UObject {
  Object getProperty(String key);

  void setProperty(String key, Object value);
}
