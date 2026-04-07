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

class MoveToCommandTest {

  /*
     Тест, который проверяет, что после команды MoveToCommand, поток переходит на обработку Команд мощью состояния MoveToState
  */
  @Test
  @DisplayName(
      "После MoveToCommand поток переходит в MoveToState: команды перекладываются в targetQueue")
  void shouldSwitchToMoveToStateAfterMoveToCommand() {
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
    // Act: отправляем MoveToCommand — переводит состояние в MoveToState
    sourceQueue.add(new MoveToCommand());
    // Отправляем обычную команду(FireCommand) в MoveToState она должна быть переложена в
    // targetQueue
    Command fireCommand = new FireCommand();
    sourceQueue.add(fireCommand);

    // Assert: ждём, пока команда окажется в targetQueue
    await()
        .atMost(2, TimeUnit.SECONDS)
        .untilAsserted(
            () -> {
              Command targetQueueCommand = targetQueue.peek();
              assertThat(targetQueueCommand)
                  .as("Обычная команда должна быть переложена в targetQueue (MoveToState)")
                  .isSameAs(fireCommand);
            });

    // Останавливаем поток
    sourceQueue.add(new HardStopCommand());
    await()
        .atMost(2, TimeUnit.SECONDS)
        .untilAsserted(() -> assertThat(serverThread.isAlive()).isFalse());
  }
}
