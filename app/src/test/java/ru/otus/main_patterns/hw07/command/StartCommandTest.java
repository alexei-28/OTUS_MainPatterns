package ru.otus.main_patterns.hw07.command;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.*;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.otus.main_patterns.hw07.ServerThread;
import ru.otus.main_patterns.hw07.interfaces.Command;

/*
    Написать тесты на команду запуска StartCommand.

    В контексте написания тестов библиотека Awaitility — это фактически «высокоуровневая обертка» над механизмами ожидания,
    которая заменяет громоздкое использование java.util.concurrent.locks.Condition.Condition, wait/notify или Thread.sleep().

    Почему Awaitility лучше Condition в тестах?
    Использование java.util.concurrent.locks.Condition в коде теста избыточно и сложно. Вам пришлось бы:
     1. Создавать Lock и Condition внутри теста.
     2. Оборачивать проверку в try-finally.
     3. Заставлять основной поток теста засыпать через await().
     4. Модифицировать боевой код (например, ServerThread), чтобы он вызывал signal(), когда задача выполнена.
        Awaitility делает это проще: она сама опрашивает (polling) состояние вашей переменной или объекта в фоновом режиме,
        не требуя менять логику вашего ServerThread.

    Когда можно заменить Condition на Awaitility?
    Если вы используете Condition только для ожидания события в тесте, то Awaitility — лучший вариант.
    Библиотека Awaitility сама берет на себя цикл ожидания и проверку условий.
*/
class StartCommandTest {

  // Тест, который проверяет, что после команды StartCommand, поток запущен.
  @Test
  void startCommand_shouldStartServerThread() throws InterruptedException {
    // Arrange
    BlockingQueue<Command> queue = new ArrayBlockingQueue<>(10);
    ServerThread serverThread = new ServerThread(queue);
    // Проверяем, что поток ещё не запущен
    assertThat(serverThread.isAlive())
        .as("ServerThread не должен быть запущен до execute()")
        .isFalse();

    StartCommand startCommand = new StartCommand(serverThread);
    // Act
    startCommand.execute();

    // Assert
    // Ждем, пока поток станет alive, максимум 2 секунды
    await()
        .atMost(2, TimeUnit.SECONDS)
        .untilAsserted(
            () ->
                assertThat(serverThread.isAlive())
                    .as("ServerThread должен быть запущен после StartCommand")
                    .isTrue());

    // Останавливаем поток, чтобы тест корректно завершился
    serverThread.stop();
  }

  /*
    Тест, который проверяет, что после команды старт поток запущен.
    Используются условные события синхронизации.
  */
  @Test
  @DisplayName(
      "Проверка запуска потока: команда в очереди должна быть выполнена после StartCommand")
  void shouldExecuteCommandFromQueueAfterStartCommandInvoked() {
    // Arrange
    BlockingQueue<Command> queue = new ArrayBlockingQueue<>(10);
    ServerThread serverThread = new ServerThread(queue);
    StartCommand startCommand = new StartCommand(serverThread);

    // Условное событие: флаг, который изменится внутри потока сервера
    AtomicBoolean isCommandExecuted = new AtomicBoolean(false);

    // Создаем тестовую команду (используем функциональный интерфейс Command)
    Command testCommand = () -> isCommandExecuted.set(true);

    // Act
    // Выполняем команду старта (она вызывает serverThread.start())
    startCommand.execute();

    // Кладем задачу в очередь. Если поток запущен, он ее подхватит.
    queue.add(testCommand);

    // Assert
    // Awaitility ожидает выполнения условия в другом потоке (polling)
    await()
        .atMost(2, SECONDS) // Таймаут, чтобы тест не завис
        .untilAsserted(
            () ->
                assertThat(isCommandExecuted.get())
                    .as("Поток сервера должен был запуститься и выставить флаг в true")
                    .isTrue());

    // Очистка ресурсов
    serverThread.stop();
  }
}
