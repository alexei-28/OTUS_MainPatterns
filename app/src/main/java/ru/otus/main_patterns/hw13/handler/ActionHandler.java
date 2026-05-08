package ru.otus.main_patterns.hw13.handler;

import ru.otus.main_patterns.hw13.UObject;
import ru.otus.main_patterns.hw13.command.Command;

/**
 * Стратегия преобразования DSL-приказа (order) в исполняемую {@link Command}.
 *
 * <p>Каждая реализация отвечает за конкретный тип действия (например, {@code StartMove}, {@code
 * Shoot}, {@code BurnFuel}) и регистрируется в {@link ru.otus.main_patterns.hw13.ActionRegistry}
 * под соответствующим строковым ключом.
 *
 * <p>Вызывается из {@link ru.otus.main_patterns.hw13.command.InterpretCommand} при интерпретации
 * игрового приказа.
 *
 * @see ru.otus.main_patterns.hw13.ActionRegistry
 * @see ru.otus.main_patterns.hw13.command.InterpretCommand
 */
public interface ActionHandler {

  /**
   * Преобразует приказ в команду для указанного игрового объекта.
   *
   * @param order DSL-приказ, содержащий параметры действия
   * @param gameObject игровой объект, к которому применяется действие
   * @return готовая к выполнению команда
   */
  Command handle(UObject order, UObject gameObject);
}
