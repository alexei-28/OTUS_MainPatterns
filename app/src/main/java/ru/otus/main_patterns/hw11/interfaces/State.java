package ru.otus.main_patterns.hw11.interfaces;

import ru.otus.main_patterns.hw11.Context;

public interface State {

  /** - Возвращает следующее состояние или самого себя если состояние не меняется. */
  State handle(Context context);
}
