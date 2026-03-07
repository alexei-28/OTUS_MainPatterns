package ru.otus.main_patterns.hw07.command;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.awaitility.Awaitility.await;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.otus.main_patterns.hw07.ServerThread;
import ru.otus.main_patterns.hw07.interfaces.Command;

class HardStopCommandTest {

  /*
      Тест, который проверяет, что после команды HardStopCommand, поток завершается
  */
  @Test
  @DisplayName("Проверка остановки потока: после HardStopCommand поток должен завершиться")
  void shouldStopServerThreadWhenCallHardStopCommand() {
    // Arrange
    BlockingQueue<Command> queue = new ArrayBlockingQueue<>(10);
    ServerThread serverThread = new ServerThread(queue);
    // Проверяем, что поток ещё не запущен
    assertThat(serverThread.isAlive())
        .as("ServerThread не должен быть запущен до execute()")
        .isFalse();
    StartCommand startCommand = new StartCommand(serverThread);
    startCommand.execute();
    Command hardStop = new HardStopCommand(serverThread);

    // Act
    queue.add(hardStop);

    // Assert
    // Ждем, пока поток станет alive, максимум 2 секунды
    await()
        .atMost(2, TimeUnit.SECONDS)
        .untilAsserted(
            () ->
                assertThat(serverThread.isAlive())
                    .as("ServerThread должен завершиться после HardStopCommand")
                    .isFalse());
  }
}
