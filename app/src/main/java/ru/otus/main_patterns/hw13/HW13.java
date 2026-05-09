package ru.otus.main_patterns.hw13;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.main_patterns.hw13.command.*;
import ru.otus.main_patterns.hw13.handler.BurnFuelHandler;
import ru.otus.main_patterns.hw13.handler.ShootHandler;
import ru.otus.main_patterns.hw13.handler.StartMoveHandler;
import ru.otus.main_patterns.hw13.handler.StopMoveHandler;

/*
  Домашнее задание:
  Разработать систему команд для космических кораблей (pattern Интерпретатор)

  Цель:
  Научиться применять интерпретатор.
  Необходимо научить игровые объекты реагировать на действия игроков.

  Описание/Пошаговая инструкция выполнения домашнего задания:
  Предположим, что приказ имеет следующий вид:
  {
    "ID": "ид объекта, которому адресован приказ",
    "action": "действие, которое необходимо выполнить",
    // какие-то специфичные параметры для данного приказа
  }

  Например, вот так может выглядеть приказ кораблю с id "548" начать двигаться по прямой со скоростью 2.
  {
    "id": "548",
    "action": "StartMove",
    "initialVelocity": 2
  }

  Приказы будем представлять также, как и игровые объекты, с помощью интерфейса UObject
  Необходимо реализовать:
  1. Команду, которая получает на вход приказ и с помощью IoC выполняет необходимое действие.
  2. Необходимо запретить одному игроку отдавать приказы другому игроку.

  Критерии оценки:
  1. Задача сдана на проверку. 1 балл
  2. Оформлен MR/PR. 1 балл
  3. CI. 1 балл.
  4. Реализован интерпретатор приказов из п. 1.
    1 балл, если с помощью этого интерпретатора можно обработать приказы на старт/стоп движения, выстрел.
    2 балла, если преподаватель не может привести пример приказа для управления игровыми объектами,
      для реализации которого требуется изменить код.
    3 балла, если можно обрабатывать не только приказы игровым объектам, но и любе другие.
    1 балл, если код покрыт тестами.
  5. Реализована защита, которая не позволяет отдавать приказы чужим объектам.
    1 балл, если защита реализована любым способом.
    2 балла, если защита реализована через отдельные скоупы для каждого игрока.
    1 балл, если код покрыт тестами.

  Максимальная оценка в 10 баллов
  Задание принято, если оценено не менее, чем в 7 баллов

  Решение:
  Паттерн Интерпретатор берёт выражение на некотором языке (DSL) и преобразует его в смысл (поведение)

  Общая идея:
  Здесь не просто “команды корабля”, а мини-язык управления космическими объектами (DSL):
    order:
    {
      id = "548"
      action = "StartMove"
      initialVelocity = 2
    }

    Это по сути скрипт/инструкция, которую система должна:
    1. Прочитать
    2. Понять (интерпретировать)
    3. Превратить в действия
    4. Выполнить

    Схема:
    UObject (DSL)
       ↓
    InterpretCommand (Interpreter)
       ↓
    ActionRegistry (семантика action)
       ↓
    ActionHandler (построение команды)
       ↓
    IoC (создание Command)
       ↓
    Command.execute()

  Итог:
   Мини-язык управления кораблями + система "язык -> интерпретация -> исполнение".
    Command Pattern:
     - StartMoveCommand
     - StopMoveCommand
    Interpreter Pattern:
     - InterpretCommand ← главный интерпретатор
     - ActionRegistry ← грамматика
     - ActionHandler ← семантика
     - UObject order ← AST/DSL
    IoC:
     - фабрика команд
     - dependency injection

    InterpretCommand реализует паттерн Interpreter, так как он принимает входной DSL-объект (UObject),
    интерпретирует его поле action как символ языка, находит соответствующее правило в ActionRegistry
    и преобразует его в исполняемую Command. Таким образом он отделяет синтаксис (UObject) от семантики (Command execution).
*/
public class HW13 {
  private static final Logger logger = LoggerFactory.getLogger(HW13.class);

  public static void main(String[] vars) {
    logger.info(
        "Java version: {}, Java vendor: {}\nРазработать систему команд для космических кораблей",
        System.getProperty("java.version"),
        System.getProperty("java.vendor"));

    initRegistry();

    String playerId = "player-1";

    // создаём scope игрока
    IoC.createScope(playerId);

    // СОЗДАЁМ И РЕГИСТРИРУЕМ КОРАБЛЬ В SCOPE
    UObject ship = new GameObject();
    ship.setProperty("id", "548");
    ship.setProperty("ownerId", playerId);

    // scoped storage внутри IoC
    IoC.register(
        playerId,
        "Ships.Storage",
        args -> {
          Map<String, UObject> ships = new HashMap<>();
          ships.put("548", ship);
          return ships;
        });

    // доступ к объектам через scope
    IoC.register(
        playerId,
        "Objects.GetById",
        args -> {
          String id = (String) args[0];
          Map<String, UObject> ships = IoC.resolve(playerId, "Ships.Storage");
          return ships.get(id);
        });

    // DSL приказ
    UObject order = new GameObject();
    order.setProperty("id", "548");
    order.setProperty("action", "StartMove");
    order.setProperty("initialVelocity", 2);

    // интерпретация и выполнение
    Command command = new InterpretCommand(order, playerId);
    command.execute();
    logger.debug("Finish executed command, playerId = {}", playerId);
  }

  private static void initRegistry() {
    // COMMAND FACTORIES (GLOBAL IoC)
    IoC.register(
        "Commands.StartMove", args -> new StartMoveCommand((UObject) args[0], (int) args[1]));

    IoC.register(
        "Commands.BurnFuel", args -> new BurnFuelCommand((UObject) args[0], (int) args[1]));

    IoC.register(
        "Commands.Shoot",
        args -> new ShootCommand((UObject) args[0], (String) args[1], (String) args[2]));

    IoC.register("Commands.StopMove", args -> new StopMoveCommand((UObject) args[0]));

    // ACTION REGISTRY (DSL → handler)
    ActionRegistry.register("StartMove", new StartMoveHandler());
    ActionRegistry.register("BurnFuel", new BurnFuelHandler());
    ActionRegistry.register("Shoot", new ShootHandler());
    ActionRegistry.register("StopMove", new StopMoveHandler());
  }
}
