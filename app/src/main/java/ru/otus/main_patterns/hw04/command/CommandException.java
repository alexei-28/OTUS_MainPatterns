package ru.otus.main_patterns.hw04.command;

public class CommandException extends RuntimeException {
    public CommandException() {
    }

    public CommandException(Throwable cause) {
        super(cause);
    }
}
