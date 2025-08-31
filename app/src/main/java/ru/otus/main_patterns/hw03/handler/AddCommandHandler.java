package ru.otus.main_patterns.hw03.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.main_patterns.hw03.EventLoopService;
import ru.otus.main_patterns.hw03.command.Command;
import ru.otus.main_patterns.hw03.command.EmptyCommand;

import java.util.function.BiFunction;

public class AddCommandHandler  implements BiFunction<Command, Exception, Command> {
    private final EventLoopService eventLoopService;
    private final Command command;
    private static final Logger logger = LoggerFactory.getLogger(AddCommandHandler.class);

    public AddCommandHandler(EventLoopService eventLoopService, Command command) {
        this.eventLoopService = eventLoopService;
        this.command = command;
    }

    @Override
    public Command apply(Command failCommand, Exception exception) {
        logger.debug("Add command {}", command);
        eventLoopService.addCommand(command);
        EmptyCommand successCommand = new EmptyCommand();
        logger.debug("failCommand = {}, exception = {} -> successCommand = {}", failCommand, exception.getClass().getSimpleName(), successCommand);
        return successCommand;
    }
}