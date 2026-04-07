package ru.otus.main_patterns.hw10.queue;

import java.util.concurrent.BlockingQueue;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.main_patterns.hw10.command.Command;

public class ServerQueueThread {
  private final BlockingQueue<Command> blockingQueue;
  private volatile Supplier<Boolean> stopStrategy = () -> false;
  private final Thread thread;
  private static final Logger logger = LoggerFactory.getLogger(ServerQueueThread.class);

  public ServerQueueThread(BlockingQueue<Command> blockingQueue) {
    this.blockingQueue = blockingQueue;

    thread =
        new Thread(
            () -> {
              // EventLoop -  выполняет команды из очереди.
              // Теперь цикл зависит от результата выполнения метода get()
              while (!stopStrategy.get()) {
                Command command = null;
                try {
                  command = blockingQueue.take(); // блокирует, если очередь пуста
                  logger.debug("Executing {}", command.getClass().getSimpleName());
                  command.execute();
                } catch (Exception e) {
                  logger.error("ServerThread, fail execution command: " + command, e);
                }
              }
            });
    thread.setName("ServerThread");
  }

  public void start() {
    logger.debug("start");
    thread.start();
  }

  // Обычная остановка (Hard Stop)
  public void stop() {
    logger.debug("stop");
    this.stopStrategy = () -> true;
  }

  /*
    Можно ли использовать просто boolean? Да, но тогда логика проверки "пуста ли очередь" при Soft Stop переедет внутрь
    самого цикла while в ServerThread.
    Минус подхода с boolean флагом: Класс ServerThread "знает", что такое Soft Stop/Hard stop.
    Если завтра захотим добавить "Stop After 10 Minutes", нам придется менять код самого сервера.

    Решение:
    Паттерн "Стратегия"(by functional interface Suppler)  для управления циклом остановки, что делает код гибким.
    Supplier - это лучше, так как это делает код более расширяемым (SOLID.
    и позволяет SoftStopCommand самой определять условия завершения, не перегружая класс ServerThread лишней логикой.
    Вариант через Supplier более гибкий, чем просто boolean флаг.
    Supplier позволяет нам динамически менять стратегию остановки. Можно на лету менять само определение того, что значит "остановиться".
  */
  public void setStopStrategy(Supplier<Boolean> stopStrategy) {
    this.stopStrategy = stopStrategy;
  }

  // wait for thread to finish
  public void join() throws InterruptedException {
    thread.join();
  }

  public boolean isAlive() {
    return thread.isAlive();
  }
}
