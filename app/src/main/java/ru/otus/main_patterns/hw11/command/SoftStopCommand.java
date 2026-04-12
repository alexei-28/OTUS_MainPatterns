package ru.otus.main_patterns.hw11.command;

import java.util.concurrent.BlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.main_patterns.hw11.ServerThread;
import ru.otus.main_patterns.hw11.interfaces.Command;

public class SoftStopCommand implements Command {
  private final BlockingQueue<Command> blockingQueue;
  private final ServerThread serverThread;
  private static final Logger logger = LoggerFactory.getLogger(SoftStopCommand.class);

  public SoftStopCommand(BlockingQueue<Command> blockingQueue, ServerThread serverThread) {
    this.blockingQueue = blockingQueue;
    this.serverThread = serverThread;
  }

  @Override
  public void execute() {
    logger.debug("\nSoftCommand, execute");
    serverThread.setStopStrategy(blockingQueue::isEmpty);
  }
}
