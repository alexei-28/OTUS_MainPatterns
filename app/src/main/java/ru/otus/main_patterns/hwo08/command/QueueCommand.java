package ru.otus.main_patterns.hwo08.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.main_patterns.hwo08.service.QueueService;

public class QueueCommand implements Command {
  private final Command command;
  private final QueueService queueService;

  private static final Logger logger = LoggerFactory.getLogger(QueueCommand.class.getName());

  public QueueCommand(Command command) {
    this.command = command;
    this.queueService = QueueService.getInstance();
    queueService.initAndStartServerQueue();
    logger.debug("Constructor QueueCommand, queueService: {}", queueService);
  }

  @Override
  public void execute() {
    queueService.addCommand(command);
  }
}
