package ru.otus.main_patterns.hw03.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.main_patterns.hw03.EventLoopService;
import ru.otus.main_patterns.hw03.command.Command;
import ru.otus.main_patterns.hw03.command.EmptyCommand;
import ru.otus.main_patterns.hw03.command.FirstTimeCommand;
import ru.otus.main_patterns.hw03.command.LogCommand;
import ru.otus.main_patterns.hw03.command.SecondTimeCommand;

import java.util.function.BiFunction;

//  Стратегия, если исключение вылетает на FirstTimeCommand, то мы создаём SecondTimeCommand во внутрь которой вставляем FirstTimeCommand.
public class DoubleRetryAndLogHandler implements BiFunction<Command, Exception, Command> {
    private final EventLoopService eventLoopService;
    private static final Logger logger = LoggerFactory.getLogger(DoubleRetryAndLogHandler.class);

    public DoubleRetryAndLogHandler(EventLoopService eventLoopService) {
        this.eventLoopService = eventLoopService;
    }

    @Override
    public Command apply(Command failCommand, Exception exception) {
        logger.debug("failCommand: {}, exception: {}", failCommand, exception.getClass().getSimpleName());
        Command successCommand = new EmptyCommand();
        if (failCommand instanceof FirstTimeCommand) {
            SecondTimeCommand secondTimeCommand = new SecondTimeCommand((FirstTimeCommand) failCommand);
            eventLoopService.addCommand(secondTimeCommand);
            logger.debug("-> successCommand: {}", successCommand);

            ExceptionHandler.registerHandler(secondTimeCommand, exception, this);

            return successCommand;
        } else if (failCommand instanceof SecondTimeCommand) {
            successCommand = new LogCommand(exception);
            logger.debug("-> successCommand: {}", successCommand);
            return successCommand;
        }
        FirstTimeCommand firstTimeCommand = new FirstTimeCommand(failCommand);
        eventLoopService.addCommand(firstTimeCommand);
        logger.debug("-> successCommand: {}", successCommand);

        ExceptionHandler.registerHandler(firstTimeCommand, exception, this);

        return successCommand;
    }
}
