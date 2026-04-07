package ru.otus.main_patterns.hw10.service;

import ru.otus.main_patterns.hw10.command.InterpretCommand;
import ru.otus.main_patterns.hw10.dto.Order;

public class InterpretCommandService {

  public void processMessage(Order order) {
    InterpretCommand interpretCommand = new InterpretCommand(order);
    interpretCommand.execute();
  }
}
