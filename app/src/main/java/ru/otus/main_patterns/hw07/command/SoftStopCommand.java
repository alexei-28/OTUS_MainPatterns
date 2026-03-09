package ru.otus.main_patterns.hw07.command;

import java.util.concurrent.BlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.main_patterns.hw07.ServerThread;
import ru.otus.main_patterns.hw07.interfaces.Command;

/*
   4. Написать команду, которая останавливает цикл выполнения команд из пункта 1, только после того,
     как все команды завершат свою работу (soft stop).
*/
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
