package ru.otus.main_patterns.hw03.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** RetryCommand повторяет команду command. */
public class RetryCommand implements Command {
  private static final Logger logger = LoggerFactory.getLogger(RetryCommand.class);

  private final Command command;

  public RetryCommand(Command command) {
    this.command = command;
  }

  /*-
      Если метод execute() выбрасывает исключение, то оно не должно обрабатываться по месту.
      Кроме закрытие ресурсов (например закрытие файла). Для этого можно использовать конструкцию try-with-resource
  */
  @Override
  public void execute() {
    logger.debug("Retry command = {}", command);
    command.execute();
  }

  @Override
  public String toString() {
    return "RetryCommand {" + "command = " + command + '}';
  }
}
