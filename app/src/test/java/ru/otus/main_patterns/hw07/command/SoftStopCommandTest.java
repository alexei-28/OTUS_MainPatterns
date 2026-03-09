package ru.otus.main_patterns.hw07.command;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.awaitility.Awaitility.await;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.otus.main_patterns.hw07.ServerThread;
import ru.otus.main_patterns.hw07.interfaces.Command;

class SoftStopCommandTest {

  /*
     Тест, который проверяет, что после команды SoftStopCommand, ServerThread завершается только после того, как все задачи закончились
  */
  @Test
  @DisplayName(
      "SoftStopCommand должен завершить поток только после обработки всех оставшихся задач")
  void shouldProcessAllCommandsBeforeStopServerThread() {
    // Arrange
    BlockingQueue<Command> queue = new ArrayBlockingQueue<>(100);
    ServerThread serverThread = new ServerThread(queue);
    AtomicInteger executedCount = new AtomicInteger(0);
    // Задачи, которые должны успеть выполниться ПОСЛЕ вызова SoftStopCommand
    Command testCommand1 = executedCount::incrementAndGet;
    Command testCommand2 = executedCount::incrementAndGet;
    Command testCommand3 = executedCount::incrementAndGet;
    // Запускаем сервер
    new StartCommand(serverThread).execute();

    // Act
    queue.add(new SoftStopCommand(queue, serverThread));
    queue.add(testCommand1);
    queue.add(testCommand2);
    queue.add(testCommand3);

    // Проверяем, что все задачи (всего 3 шт + сама команда стоп) выполнены
    await()
        .atMost(10, SECONDS)
        .untilAsserted(
            () ->
                assertThat(executedCount.get())
                    .as("Все задачи из очереди должны быть выполнены")
                    .isEqualTo(3));

    // Проверяем, что ПОСЛЕ выполнения задач поток завершился
    await()
        .atMost(1, SECONDS)
        .untilAsserted(
            () ->
                assertThat(serverThread.isAlive())
                    .as("Поток сервера должен завершиться после очистки очереди")
                    .isFalse());
  }
}
