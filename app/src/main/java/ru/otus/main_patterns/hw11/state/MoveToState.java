package ru.otus.main_patterns.hw11.state;

import ru.otus.main_patterns.hw11.Context;
import ru.otus.main_patterns.hw11.interfaces.State;

public class MoveToState implements State {

  @Override
  public State handle(Context context) {
    context.setState(this);
    return null;
  }
}
