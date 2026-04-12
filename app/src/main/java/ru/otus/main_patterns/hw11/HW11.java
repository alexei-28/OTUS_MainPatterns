package ru.otus.main_patterns.hw11;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.main_patterns.hw11.command.*;
import ru.otus.main_patterns.hw11.interfaces.Command;

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
*/
public class HW11 {

  private static final Logger logger = LoggerFactory.getLogger(HW11.class);

  public static void main(String[] args) {
    logger.info(
        "Java version: {}, Java vendor: {}\nДомашнее задание#11: Состояние",
        System.getProperty("java.version"),
        System.getProperty("java.vendor"));

    BlockingQueue<Command> blockingQueue = new ArrayBlockingQueue<>(100);
    ServerThread serverThread = new ServerThread(blockingQueue);
    StartCommand startCommand = new StartCommand(serverThread);
    startCommand.execute();

    blockingQueue.add(new SoftStopCommand(blockingQueue, serverThread));
    blockingQueue.add(new FillFuelCommand());
    blockingQueue.add(new LoadWeaponsCommand());
    blockingQueue.add(new ErrorCommand());
    blockingQueue.add(new MoveCommand());
    // blockingQueue.add(new HardStopCommand(serverThread));
    blockingQueue.add(new RotateCommand());
  }
}
