package ru.otus.main_patterns.hw03.command;

/**
    Повторяет команду failCommand первый раз.
    failCommand - команда, которая не смогла выполниться.
 */
public class FirstTimeCommand implements Command{
    private final Command failCommand;

    public FirstTimeCommand(Command failCommand) {
        this.failCommand = failCommand;
    }

    /*-
        Если метод execute() выбрасывает исключение, то оно не должно обрабатываться по месту.
        Кроме закрытие ресурсов (например закрытие файла). Для этого можно использовать конструкцию try-with-resource
    */
    @Override
    public void execute() {
        failCommand.execute();
    }

    @Override
    public String toString() {
        return "FirstTimeCommand {" +
                "failCommand = " + failCommand +
                '}';
    }
}
