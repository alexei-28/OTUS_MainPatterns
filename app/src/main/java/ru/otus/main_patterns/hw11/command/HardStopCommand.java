package ru.otus.main_patterns.hw11.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.main_patterns.hw11.ServerThread;
import ru.otus.main_patterns.hw11.interfaces.Command;

public class HardStopCommand implements Command {
  private final ServerThread serverThread;
  private static final Logger logger = LoggerFactory.getLogger(HardStopCommand.class);

  public HardStopCommand(ServerThread serverThread) {
    this.serverThread = serverThread;
  }

  @Override
  public void execute() {
    logger.debug("\nHardStopCommand, execute");
    serverThread.stop();
  }
}
