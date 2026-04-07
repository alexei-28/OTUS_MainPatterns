package ru.otus.main_patterns.hw11.state;

import java.util.concurrent.BlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.main_patterns.hw11.command.HardStopCommand;
import ru.otus.main_patterns.hw11.command.RunCommand;
import ru.otus.main_patterns.hw11.interfaces.Command;
import ru.otus.main_patterns.hw11.interfaces.CommandState;

public class MoveToState implements CommandState {
  private final BlockingQueue<Command> sourceQueue;
  private final BlockingQueue<Command> targetQueue;
  private static final Logger logger = LoggerFactory.getLogger(MoveToState.class);

  public MoveToState(BlockingQueue<Command> sourceQueue, BlockingQueue<Command> targetQueue) {
    this.sourceQueue = sourceQueue;
    this.targetQueue = targetQueue;
  }

  @Override
  public CommandState handle(Command command) {
    if (command instanceof HardStopCommand) {
      return null;
    }
    if (command instanceof RunCommand) {
      CommandState normalState = new NormalState(sourceQueue, targetQueue);
      logger.debug(
          "handle, {} -> change from {} to {}",
          command.getClass().getSimpleName(),
          this.getClass().getSimpleName(),
          normalState.getClass().getSimpleName());
      return normalState;
    }
    // вместо выполнения — перекладываем
    targetQueue.add(command);
    logger.debug(
        "handle, move {} to targetQueue({})",
        command.getClass().getSimpleName(),
        targetQueue.size());
    return this; // остаёмся в том же состоянии
  }
}
