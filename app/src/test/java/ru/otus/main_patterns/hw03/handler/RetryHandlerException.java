package ru.otus.main_patterns.hw03.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.main_patterns.hw03.command.Command;

import java.util.function.BiFunction;

public class RetryHandlerException implements BiFunction<Command, Exception, Command> {
    private static final Logger logger = LoggerFactory.getLogger(RetryHandlerException.class);


    @Override
    public Command apply(Command failCommand, Exception exception) {
        logger.debug("failCommand: {}, exception: {}", failCommand, exception.getClass().getSimpleName());
        return null;
    }
}
