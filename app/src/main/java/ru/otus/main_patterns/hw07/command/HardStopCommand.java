package ru.otus.main_patterns.hw07.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.main_patterns.hw07.ServerThread;
import ru.otus.main_patterns.hw07.interfaces.Command;

/*
   3. Написать команду, которая останавливает цикл выполнения команд из пункта 1, не дожидаясь их полного завершения (hard stop).
*/
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
