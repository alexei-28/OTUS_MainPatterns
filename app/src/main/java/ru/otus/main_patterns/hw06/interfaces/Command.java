package ru.otus.main_patterns.hw06.interfaces;

/** Команда. */
public interface Command {

  /**
   * Выполнить Команду. Если Команда по какой-либо причине не может быть выполнена,то выбрасывается
   * исключение.
   */
  void execute();
}
