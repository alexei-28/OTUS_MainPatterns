package ru.otus.main_patterns.hw07.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.main_patterns.hw07.ServerThread;
import ru.otus.main_patterns.hw07.interfaces.Command;

/*
   2. Написать команду, которая стартует код, написанный в пункте 1 в отдельном потоке.
*/
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
