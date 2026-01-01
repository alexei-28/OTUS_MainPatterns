package ru.otus.main_patterns.hw03.command;

/**
 * Повторяет команду FirstTimeCommand. FirstTimeCommand - команда, которая повторяет команду
 * failCommand. Т.е. SecondTimeCommand повторяет команду failCommand во второй раз.
 */
public class SecondTimeCommand implements Command {
  private final FirstTimeCommand firstTimeCommand;

  public SecondTimeCommand(FirstTimeCommand firstTimeCommand) {
    this.firstTimeCommand = firstTimeCommand;
  }

  /*-
      Если метод execute() выбрасывает исключение, то оно не должно обрабатываться по месту.
      Кроме закрытие ресурсов (например закрытие файла). Для этого можно использовать конструкцию try-with-resource
  */
  @Override
  public void execute() {
    firstTimeCommand.execute();
  }

  @Override
  public String toString() {
    return "SecondTimeCommand { " + "firstTimeCommand = " + firstTimeCommand + '}';
  }
}
