package ru.otus.main_patterns.hw11.state;

import java.util.concurrent.BlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.main_patterns.hw11.command.HardStopCommand;
import ru.otus.main_patterns.hw11.command.MoveToCommand;
import ru.otus.main_patterns.hw11.interfaces.Command;
import ru.otus.main_patterns.hw11.interfaces.CommandState;

public class NormalState implements CommandState {
  private final BlockingQueue<Command> sourceQueue;
  private final BlockingQueue<Command> targetQueue;
  private static final Logger logger = LoggerFactory.getLogger(NormalState.class);

  public NormalState(BlockingQueue<Command> sourceQueue, BlockingQueue<Command> targetQueue) {
    this.sourceQueue = sourceQueue;
    this.targetQueue = targetQueue;
  }

  @Override
  public CommandState handle(Command command) {
    if (command instanceof HardStopCommand) {
      return null;
    }
    if (command instanceof MoveToCommand) {
      CommandState moveState = new MoveToState(sourceQueue, targetQueue);
      logger.debug(
          "handle, {} -> change from {} to {}",
          command.getClass().getSimpleName(),
          this.getClass().getSimpleName(),
          moveState.getClass().getSimpleName());
      return moveState;
    }
    // обычная команда
    command.execute();
    return this; // остаёмся в том же состоянии
  }
}
