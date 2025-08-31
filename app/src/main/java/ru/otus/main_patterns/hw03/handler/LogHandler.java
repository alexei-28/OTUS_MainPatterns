package ru.otus.main_patterns.hw03.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.main_patterns.hw03.command.Command;
import ru.otus.main_patterns.hw03.command.LogCommand;

import java.util.function.BiFunction;

public class LogHandler implements BiFunction<Command, Exception, Command> {
    private static final Logger logger = LoggerFactory.getLogger(LogHandler.class);

    @Override
    public Command apply(Command failCommand, Exception exception) {
        LogCommand successCommand = new LogCommand(exception);
        logger.debug("failCommand = {}, exception = {} -> successCommand = {}", failCommand, exception.getClass().getSimpleName(), successCommand);
        return successCommand;
    }
}
