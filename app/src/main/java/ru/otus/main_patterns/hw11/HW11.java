package ru.otus.main_patterns.hw11;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.main_patterns.hw11.command.*;
import ru.otus.main_patterns.hw11.interfaces.Command;
import ru.otus.main_patterns.hw11.interfaces.CommandState;
import ru.otus.main_patterns.hw11.state.NormalState;

/*
   Домашнее задание
   Смена режимов обработки команд

   Цель:
   Применит паттерн Состояние для изменения поведения обработчиков Команд.

   Описание/Пошаговая инструкция выполнения домашнего задания:
   В домашнем задании №2 была реализована многопоточная обработка очереди команд.
   Предлагалось два режима остановки этой очереди - hard и soft.
   Однако вариантов завершения и режимов обработки может быть гораздо больше.
   В данном домашнем задании необходимо реализовать возможность смены режима обработки Команд в потоке, начиная со следующей Команды.
   Для этого предлагается использовать паттерн Состояние. Каждое состояние будет иметь свой режим обработки команд.
   Метод handle возвращает ссылку на следующее состояние.
   Необходимо реализовать следующие состояния автомата:
   1. "Обычное"
       В этом состоянии очередная команда извлекается из очереди и выполняется.
       По умолчанию возвращается ссылка на этот же экземпляр состояния.
       Обработка команды HardStop приводит к тому, что будет возвращена "нулевая ссылка" на следующее состояние,
       что соответствует завершению работы потока.
       Обработка команды MoveToCommand приводит к тому, что будет возвращена ссылка на состояние "MoveTo"
   2. "MoveTo" - состояние, в котором команды извлекаются из очереди и перенаправляются в другую очередь.
       Такое состояние может быть полезно, если хотите разгрузить сервер перед предстоящим его выключением.
       Обработка команды HardStop приводит к тому, что будет возвращена "нулевая ссылка" на следующее состояние,
       что соответствует завершению работы потока.
       Обработка команды RunCommand приводит к тому, что будет возвращена ссылка на "обычное" состояние.

   Критерии оценки:
   За выполнение каждого пункта, перечисленного ниже начисляются баллы:
    1. ДЗ сдано на проверку - 2 балла
    2. Код решения опубликован на github/gitlab - 1 балл
    3. Настроен CI - 2 балла
    4. Код компилируется без ошибок - 1 балл.
    5. Написать тест, который проверяет, что после команды hard stop, поток завершается - 1 балл
    6. Написать тест, который проверяет, что после команды MoveToCommand, поток переходит на обработку Команд
       с помощью состояния MoveTo - 1 балл
    7. Написать тест, который проверяет, что после команды RunCommand, поток переходит на обработку Команд
       с помощью состояния "Обычное" - 1 балл
    8. Код не зависит от конкретных реализаций состояний - 1 балл

    Итого: 10 баллов
    Задание считается принятым, если набрано не менее 7 баллов.
*/
public class HW11 {

  private static final Logger logger = LoggerFactory.getLogger(HW11.class);

  public static void main(String[] args) {
    logger.info(
        "Java version: {}, Java vendor: {}\nДомашнее задание#11: Состояние",
        System.getProperty("java.version"),
        System.getProperty("java.vendor"));

    BlockingQueue<Command> sourceQueue = new ArrayBlockingQueue<>(100);
    BlockingQueue<Command> targetQueue = new ArrayBlockingQueue<>(100);
    CommandState normalState = new NormalState(sourceQueue, targetQueue);
    // CommandState moveToState = new MoveToState(sourceQueue, targetQueue);
    ServerThread serverThread = new ServerThread(sourceQueue, normalState);
    new StartCommand(serverThread).execute();

    sourceQueue.add(new SoftStopCommand(sourceQueue, serverThread));
    sourceQueue.add(new LoadWeaponsCommand());
    sourceQueue.add(new FillFuelCommand());

    sourceQueue.add(new MoveToCommand()); // change to MoveToState
    sourceQueue.add(new ErrorCommand());
    // sourceQueue.add(new HardStopCommand());
    sourceQueue.add(new TeleportCommand());

    sourceQueue.add(new RunCommand()); // change to NormalState
    sourceQueue.add(new FireCommand());
    // sourceQueue.add(new HardStopCommand());
    sourceQueue.add(new RotateCommand());
  }
}
