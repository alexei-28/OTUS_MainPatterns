package ru.otus.main_patterns.hw13.command;

import ru.otus.main_patterns.hw13.ActionRegistry;
import ru.otus.main_patterns.hw13.IoC;
import ru.otus.main_patterns.hw13.UObject;
import ru.otus.main_patterns.hw13.exception.AccessDeniedException;
import ru.otus.main_patterns.hw13.handler.ActionHandler;

public class InterpretCommand implements Command {

  private final UObject order;
  private final String currentPlayerId;

  /*
     InterpretCommand интерпретирует DSL (UObject) в Command.
     InterpretCommand является интерпретатором, потому что он преобразует входной DSL (UObject приказ) в исполняемые команды,
     интерпретируя значение поля action как семантическое действие системы.
     Шаги:
       1. читает входной DSL (order)
       2. выделяет символы языка (action, id, параметры)
       3. находит смысл через registry
       4. строит execution tree (Command)
       5. запускает выполнение

       Важно:
        InterpretCommand НЕ выполняет действие.
       Он делает только “перевод текста команды в исполняемую структуру”.
       Это и есть Interpreter:
          - понять что значит "StartMove"
          - собрать объект команды
          - запустить её

     В данной реализации паттерн Интерпретатор тесно связан с Service Locator (IoC).
      Это делает систему гибкой:
     - Scopes (Области видимости): В main создается playerId. Это позволяет интерпретатору проверять права доступа.
       Если один игрок попытается "интерпретировать" приказ для корабля другого игрока, checkAccess выдаст ошибку.
     - Decoupling (Разделение): InterpretCommand не знает о существовании StartMoveCommand. Он общается только с интерфейсами.
       Это значит, что можно добавить новую команду (например, "HyperJump"), просто зарегистрировав новый хендлер,
       не меняя код самого интерпретатора.
  */
  public InterpretCommand(UObject order, String currentPlayerId) {
    this.order = order;
    this.currentPlayerId = currentPlayerId;
  }

  @Override
  public void execute() {
    String objectId = (String) order.getProperty("id");
    String action = (String) order.getProperty("action");
    UObject gameObject = IoC.resolve(currentPlayerId, "Objects.GetById", objectId);
    checkAccess(gameObject);
    Command command = buildCommand(action, gameObject);
    command.execute();
  }

  private void checkAccess(UObject gameObject) {
    String ownerId = (String) gameObject.getProperty("ownerId");
    if (!currentPlayerId.equals(ownerId)) {
      throw new AccessDeniedException("Игрок не может управлять чужим объектом");
    }
  }

  private Command buildCommand(String action, UObject gameObject) {
    ActionHandler handler = ActionRegistry.get(action);
    return handler.handle(order, gameObject);
  }
}
