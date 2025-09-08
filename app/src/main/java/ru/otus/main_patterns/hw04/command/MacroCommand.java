package ru.otus.main_patterns.hw04.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;

/*-
    Простейшая макрокоманда - при выбросе исключения вся последовательность команд приостанавливает свое выполнение
    и макрокоманда выбрасывает CommandException.
 */
public class MacroCommand implements Command{
    private final BlockingQueue<Command> blockQueue;
    private static final Logger logger = LoggerFactory.getLogger(MacroCommand.class);

    public MacroCommand(BlockingQueue<Command> blockQueue) {
        this.blockQueue = blockQueue;
    }

    @Override
    public void execute() throws CommandException {
        while (!blockQueue.isEmpty()) {
            try {
                Command command = blockQueue.take();
                command.execute();
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
                throw new CommandException(ex);
            }
       }
    }

    public BlockingQueue<Command> getBlockQueue() {
        return blockQueue;
    }

    @Override
    public String toString() {
        return "MacroCommand{" +
                "blockQueue=" + blockQueue +
                '}';
    }
}
