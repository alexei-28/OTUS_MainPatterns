package ru.otus.main_patterns.hw03.handler;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import java.util.function.BiFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.main_patterns.hw03.command.Command;

public class ExceptionHandler {
  // key = Command and Exception
  // vale = handler (as BiFunction)
  private static final Table<Command, Exception, BiFunction<Command, Exception, Command>> store =
      HashBasedTable.create();
  private static final Logger logger = LoggerFactory.getLogger(ExceptionHandler.class);

  /*-
     To prevent the class from being instantiated, you should define a non-public constructor.
     This will prevent the compiler from implicitly generating a public parameterless constructor.
  */
  private ExceptionHandler() {
    throw new IllegalStateException("Utility class");
  }

  public static void registerHandler(
      Command failCommand,
      Exception exception,
      BiFunction<Command, Exception, Command> handlerException) {
    store.put(failCommand, exception, handlerException);
    logger.debug(
        "Register new handler, failCommand: {}, exception: {}, handler: {}",
        failCommand,
        exception.getClass().getSimpleName(),
        handlerException.getClass().getSimpleName());
  }

  /**
   * Обработчик команды которая не смогла выполниться успешно. Обработка одной команды не зависит от
   * обработки другой команды. Выбирает нужный Exception handler. В результате выполнения handle()
   * можно выбрать разные стратегии обработки Exception-ов, например:
   *
   * <p>- поставить команду обратно в очередь
   *
   * <p>- записать в лог
   *
   * <p>- вывести информацию на консоль и т.д.
   *
   * <p>Метод handle() выбирает нужную стратегию обработки Exception-а и возвращает команду, которая
   * вернет систему в работоспособное состояние.
   *
   * @param failCommand команда, которая не смогла выполниться успешно (e.g. RotateCommand).
   * @param exception какой exception выбросила команда (e.g. GetAngleException).
   * @return возвращает команду, которая вернет систему в работоспособное состояние.
   */
  public static Command handle(Command failCommand, Exception exception) {
    logger.debug(
        "Finding handler, failCommand: {}, exception: {}",
        failCommand,
        exception.getClass().getSimpleName());
    BiFunction<Command, Exception, Command> handler = store.get(failCommand, exception);
    logger.debug(
        "failCommand = {}, exception = {} -> success find handler = {}",
        failCommand,
        exception.getClass().getSimpleName(),
        handler.getClass().getSimpleName());
    Command successCommand = handler.apply(failCommand, exception);
    logger.debug(
        "handler = {} -> successCommand = {}", handler.getClass().getSimpleName(), successCommand);
    return successCommand;
  }
}
