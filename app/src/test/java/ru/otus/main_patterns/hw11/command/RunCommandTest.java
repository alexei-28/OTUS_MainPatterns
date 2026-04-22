package ru.otus.main_patterns.hw11.command;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.awaitility.Awaitility.await;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.otus.main_patterns.hw11.ServerThread;
import ru.otus.main_patterns.hw11.interfaces.Command;
import ru.otus.main_patterns.hw11.state.NormalState;

class RunCommandTest {

  /*
    Тест, который проверяет, что после команды RunCommand, поток переходит на обработку Команд с помощью состояния NormalState
  */
  @Test
  @DisplayName(
      "После RunCommand поток переходит в NormalState: команды выполняются, а не перекладываются")
  void shouldSwitchToNormalStateAfterRunCommand() {
    // Arrange
    BlockingQueue<Command> sourceQueue = new ArrayBlockingQueue<>(10);
    BlockingQueue<Command> targetQueue = new ArrayBlockingQueue<>(10);
    NormalState normalState = new NormalState(sourceQueue, targetQueue);
    ServerThread serverThread = new ServerThread(sourceQueue, normalState);

    AtomicBoolean commandExecuted = new AtomicBoolean(false);

    // Проверяем, что поток ещё не запущен
    assertThat(serverThread.isAlive())
        .as("ServerThread не должен быть запущен до execute()")
        .isFalse();

    new StartCommand(serverThread).execute();
    // Переводит состояние в MoveToState
    sourceQueue.add(new MoveToCommand());

    // Act: отправляем RunCommand — переводит состояние обратно в NormalState
    sourceQueue.add(new RunCommand());
    // Отправляем обычную команду — в NormalState она должна быть выполнена, а не переложена
    sourceQueue.add((Command) () -> commandExecuted.set(true));

    // Assert: ждём, пока команда выполнится (признак NormalState)
    await()
        .atMost(2, TimeUnit.SECONDS)
        .untilAsserted(
            () -> {
              assertThat(commandExecuted.get())
                  .as("Команда должна быть выполнена (NormalState), а не переложена")
                  .isTrue();
              assertThat(targetQueue.peek())
                  .as("В targetQueue не должно быть команды — она выполнена, а не переложена")
                  .isNull();
            });

    // Останавливаем поток
    sourceQueue.add(new HardStopCommand());
    await()
        .atMost(2, TimeUnit.SECONDS)
        .untilAsserted(() -> assertThat(serverThread.isAlive()).isFalse());
  }
}
