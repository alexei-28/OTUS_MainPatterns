package ru.otus.main_patterns.hw03.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.main_patterns.hw03.EventLoopService;
import ru.otus.main_patterns.hw03.command.Command;
import ru.otus.main_patterns.hw03.command.EmptyCommand;
import ru.otus.main_patterns.hw03.command.RetryCommand;

import java.util.function.BiFunction;

public class RetryHandler implements BiFunction<Command, Exception, Command> {
    private final EventLoopService eventLoopService;
    private static final Logger logger = LoggerFactory.getLogger(RetryHandler.class);

    public RetryHandler(EventLoopService eventLoopService) {
        this.eventLoopService = eventLoopService;
    }

    @Override
    public Command apply(Command failCommand, Exception exception) {
        Command successCommand = new EmptyCommand();
        RetryCommand retryCommand = new RetryCommand(failCommand);
        logger.debug("failCommand: {}, exception:{}  -> add command retryCommand:{}, successCommand: {}", failCommand, exception.getClass().getSimpleName(), retryCommand, successCommand);
        eventLoopService.addCommand(retryCommand);
        return successCommand;
    }
}
