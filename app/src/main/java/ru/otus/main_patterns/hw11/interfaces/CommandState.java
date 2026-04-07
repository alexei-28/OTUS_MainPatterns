package ru.otus.main_patterns.hw11.interfaces;

/** Каждое состояние будет иметь свой режим обработки команд */
public interface CommandState {

  /** - Возвращает следующее состояние или самого себя если состояние не меняется. */
  CommandState handle(Command command);
}
