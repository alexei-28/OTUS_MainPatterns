package ru.otus.main_patterns.hw11.command;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.awaitility.Awaitility.await;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.otus.main_patterns.hw11.ServerThread;
import ru.otus.main_patterns.hw11.interfaces.Command;
import ru.otus.main_patterns.hw11.state.NormalState;

class HardStopCommandTest {

  /*
     Тест, который проверяет, что после команды hard stop, поток завершается
  */
  @Test
  @DisplayName("Проверка остановки потока: после HardStopCommand поток должен завершиться")
  void shouldStopServerThreadWhenHardStopCommandExecute() {
    // Arrange
    BlockingQueue<Command> sourceQueue = new ArrayBlockingQueue<>(10);
    BlockingQueue<Command> targetQueue = new ArrayBlockingQueue<>(10);
    NormalState normalState = new NormalState(sourceQueue, targetQueue);
    ServerThread serverThread = new ServerThread(sourceQueue, normalState);

    // Проверяем, что поток ещё не запущен
    assertThat(serverThread.isAlive())
        .as("ServerThread не должен быть запущен до execute()")
        .isFalse();

    new StartCommand(serverThread).execute();

    // Act
    sourceQueue.add(new HardStopCommand());

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
