package ru.otus.main_patterns.hw11.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.main_patterns.hw11.ServerThread;
import ru.otus.main_patterns.hw11.interfaces.Command;

public class StartCommand implements Command {
  private ServerThread serverThread;
  private static final Logger logger = LoggerFactory.getLogger(StartCommand.class);

  public StartCommand(ServerThread serverThread) {
    this.serverThread = serverThread;
  }

  @Override
  public void execute() {
    logger.debug("\nStartCommand, execute");
    serverThread.start();
  }
}
