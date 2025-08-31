package ru.otus.main_patterns.hw03.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
  Команда, которая ничего не делает.
  Может использоваться в тестах.
 */
public class EmptyCommand implements Command{
    private static final Logger logger = LoggerFactory.getLogger(EmptyCommand.class);

    /*-
        Если метод execute() выбрасывает исключение, то оно не должно обрабатываться по месту.
        Кроме закрытие ресурсов (например закрытие файла). Для этого можно использовать конструкцию try-with-resource
    */
    @Override
    public void execute() {
        logger.debug("Executing command: {}", this.getClass().getSimpleName());
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }
}
