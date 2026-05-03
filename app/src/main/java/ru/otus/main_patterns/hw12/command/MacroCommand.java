package ru.otus.main_patterns.hw12.command;

import java.util.ArrayList;
import java.util.List;
import ru.otus.main_patterns.hw12.interfaces.Command;

public class MacroCommand implements Command {
  private final List<Command> commands = new ArrayList<>();

  public void add(Command c) {
    commands.add(c);
  }

  public void clear() {
    commands.clear();
  }

  public List<Command> getCommands() {
    return commands;
  }

  @Override
  public void execute() {
    for (Command command : commands) {
      command.execute();
    }
  }
}
