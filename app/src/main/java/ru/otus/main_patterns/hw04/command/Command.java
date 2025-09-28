package ru.otus.main_patterns.hw04.command;

public interface Command {

    /**
     Важно!
     Если метод execute() выбрасывает исключение, то оно не должно обрабатываться по месту.
     Кроме закрытие ресурсов (например закрытие файла). Для этого можно использовать конструкцию try-with-resource.
     </p>
     Исключение нужно выбрасывать наверх.
     Исключение должно обрабатываться только в одном месте - например, EventLoopService#eventLoop - секция catch {}.
     Так как команда состоит из одного метода void execute(), то ее можно заменить на, встроенный в Java, функциональный интерфейс Consumer.
     */
    public void execute() throws InterruptedException;
}