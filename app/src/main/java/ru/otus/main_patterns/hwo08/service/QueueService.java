package ru.otus.main_patterns.hwo08.service;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.main_patterns.hwo08.command.Command;
import ru.otus.main_patterns.hwo08.command.StartCommand;
import ru.otus.main_patterns.hwo08.queue.ServerQueueThread;

public class QueueService {
  private static final QueueService INSTANCE = new QueueService();
  private final BlockingQueue<Command> blockingQueue = new ArrayBlockingQueue<>(100);
  private static final Logger logger = LoggerFactory.getLogger(QueueService.class);

  private QueueService() {
    ServerQueueThread serverThread = new ServerQueueThread(blockingQueue);
    StartCommand startCommand = new StartCommand(serverThread);
    startCommand.execute();
  }

  public static QueueService getInstance() {
    return INSTANCE;
  }

  public void addCommand(Command command) {
    logger.debug("addCommand, add command {} to queue", command.getClass().getSimpleName());
    blockingQueue.add(command);
    printQueue();
  }

  private void printQueue() {
    StringBuilder sb = new StringBuilder();
    int i = 0;
    for (Command cmd : blockingQueue) {
      sb.append("Queue[")
          .append(i++)
          .append("] = ")
          .append(cmd.getClass().getSimpleName())
          .append("\n");
    }
    logger.debug("\n{}", sb);
  }
}
