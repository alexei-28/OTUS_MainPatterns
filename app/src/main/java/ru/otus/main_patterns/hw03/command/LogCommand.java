package ru.otus.main_patterns.hw03.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LogCommand - команда, которая записывает информацию о выброшенном исключении в лог(console/file).
 */
public class LogCommand implements Command {
  private final Exception exception;
  private static final Logger logger = LoggerFactory.getLogger(LogCommand.class);

  public LogCommand(Exception exception) {
    this.exception = exception;
  }

  /*-
      Если метод execute() выбрасывает исключение, то оно не должно обрабатываться по месту.
      Кроме закрытие ресурсов (например закрытие файла). Для этого можно использовать конструкцию try-with-resource
  */
  @Override
  public void execute() {
    logger.error("**************** LOG ****************\n" + exception.getMessage(), exception);
  }

  public Exception getException() {
    return exception;
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName();
  }
}
