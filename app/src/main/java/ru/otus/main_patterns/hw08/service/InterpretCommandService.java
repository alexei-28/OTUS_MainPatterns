package ru.otus.main_patterns.hw08.service;

import ru.otus.main_patterns.hw08.command.InterpretCommand;
import ru.otus.main_patterns.hw08.dto.Order;

public class InterpretCommandService {

  public void processMessage(Order order) {
    InterpretCommand interpretCommand = new InterpretCommand(order);
    interpretCommand.execute();
  }
}
