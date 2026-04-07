package ru.otus.main_patterns.hw11;

import java.util.concurrent.BlockingQueue;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.main_patterns.hw11.interfaces.Command;
import ru.otus.main_patterns.hw11.interfaces.CommandState;

public class ServerThread {
  private final BlockingQueue<Command> sourceQueue;
  private volatile Supplier<Boolean> stopStrategy = () -> false;
  private final Thread thread;
  private static final Logger logger = LoggerFactory.getLogger(ServerThread.class);

  /*
    Код не зависит от конкретных реализаций состояний.
    При добавлении новой реализаций CommandState, наш класс ServerThread не меняется.
    Таким образом мы соблюдаем "Open Closed Principle".
  */

  public ServerThread(BlockingQueue<Command> sourceQueue, CommandState commandState) {
    this.sourceQueue = sourceQueue;

    thread =
        new Thread(
            new Runnable() {
              private CommandState state = commandState;

              @Override
              public void run() {
                logger.debug("ServerThread, state: {}", state);
                while (!stopStrategy.get() && state != null) {
                  Command command = null;
                  try {
                    command = sourceQueue.take();
                    state = state.handle(command); // код не меняется
                  } catch (Exception e) {
                    logger.error("ServerThread, fail execution command: " + command, e);
                  }
                }
                logger.debug(
                    "ServerThread, finish\nsourceQueue({}) {}", sourceQueue.size(), sourceQueue);
              }
            });
    thread.setName("ServerThread");
  }

  public void start() {
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
    Паттерн "Стратегия"(by functional interface Suppler) для управления циклом остановки, что делает код гибким.
    Supplier - это лучше, так как это делает код более расширяемым (SOLID).
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
