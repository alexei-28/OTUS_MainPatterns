package ru.otus.main_patterns.hw10.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.main_patterns.hw10.queue.ServerQueueThread;

//  Команда, которая стартует ServerQueueThread в отдельном потоке.
public class StartCommand implements Command {
  private ServerQueueThread serverThread;
  private static final Logger logger = LoggerFactory.getLogger(StartCommand.class);

  public StartCommand(ServerQueueThread serverThread) {
    this.serverThread = serverThread;
  }

  @Override
  public void execute() {
    logger.debug("\nStartCommand, execute");
    serverThread.start();
  }
}
