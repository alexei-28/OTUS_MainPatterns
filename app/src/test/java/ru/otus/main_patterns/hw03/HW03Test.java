package ru.otus.main_patterns.hw03;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.*;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.main_patterns.hw03.command.EmptyCommand;
import ru.otus.main_patterns.hw03.command.FireCommand;
import ru.otus.main_patterns.hw03.command.LogCommand;
import ru.otus.main_patterns.hw03.command.MoveCommand;
import ru.otus.main_patterns.hw03.command.RetryCommand;
import ru.otus.main_patterns.hw03.command.RotateCommand;
import ru.otus.main_patterns.hw03.command.StopCommand;
import ru.otus.main_patterns.hw03.exception.GetAngleException;
import ru.otus.main_patterns.hw03.handler.AddCommandHandler;
import ru.otus.main_patterns.hw03.handler.DoubleRetryAndLogHandler;
import ru.otus.main_patterns.hw03.handler.ExceptionHandler;
import ru.otus.main_patterns.hw03.handler.LogHandler;
import ru.otus.main_patterns.hw03.handler.RetryHandler;

/*-
   ManualResetEvent - С# -> in Java use interface Condition - https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/locks/Condition.html
   Condition Variable нужен для написания тестов(не в продакшине) для того, чтобы проверить последовательность выполнения команд.

   Запуск всех тестов в пакете "ru.otus.main_patterns.hw03":
     gradlew clean test --tests "ru.otus.main_patterns.hw03.*"
*/
class HW03Test {
  private EventLoopService eventLoopService;
  private final GetAngleException getAngleException =
      new GetAngleException("Невозможно получить угол!");

  private final MoveCommand moveCommand = new MoveCommand();
  private final EmptyCommand emptyCommand = new EmptyCommand();
  private final FireCommand fireCommand = new FireCommand();

  private final LogHandler logHandler = new LogHandler();

  private static final long TIME_OUT_MS = 10_000;

  private static final Logger logger = LoggerFactory.getLogger(HW03Test.class);

  // Запускается перед каждым тестом
  @BeforeEach
  void setup() {
    eventLoopService = new EventLoopService();
    eventLoopService.eventLoop();
  }

  @Test
  @DisplayName("Команда, которая записывает информацию о выброшенном исключении в лог")
  void shouldLogWhenThrowException() {
    // Arrange (thread "Test worker")
    Lock lock = new ReentrantLock();
    // Condition variable
    Condition cv = lock.newCondition();
    // Все команды выполняются в thread-e "EventLoop-thread".

    // StopCommand специальная команда, которая останавливает(cv.await) thread "EventLoop-thread" в
    // котором работает блокирующая очередь.
    StopCommand stopCommandSpy = spy(new StopCommand());
    // Реализация, которая заменяет логику реального метода(StopCommand#execute).
    doAnswer(
            invocation -> { // Call in thread "EventLoop-thread"
              try {
                lock.lock();
                // После выполнения cv.await() "заснет" thread "EventLoop-Thread" и из очереди(в
                // thread-e "EventLoop-Thread") перестанут вытаскиваться команды.
                logger.debug(
                    "Executing command: {}\n>>>>>>>>> Current thread goes to sleep >>>>>>>>>",
                    stopCommandSpy);
                cv.await(); // Call in thread "EventLoop-Thread"
                return null;
              } finally {
                lock.unlock();
              }
            })
        .when(stopCommandSpy)
        .execute();

    RotateCommand rotateCommandMock = mock(RotateCommand.class);
    // При выполнении метода execute команды rotateCommandMock будет выброшено исключение
    // getAngleException
    doThrow(getAngleException).when(rotateCommandMock).execute();

    LogHandler logHandlerMock = mock(LogHandler.class);
    LogCommand logCommandSpy = spy(new LogCommand(getAngleException));
    when(logHandlerMock.apply(any(), any())).thenReturn(logCommandSpy);

    // Call in thread "Test worker"
    // Добавляем команды в очередь
    eventLoopService.addCommand(stopCommandSpy); // останавливает thread "EventLoop-thread"
    eventLoopService.addCommand(moveCommand);
    eventLoopService.addCommand(rotateCommandMock); // выбрасывает getAngleException
    eventLoopService.addCommand(fireCommand);
    eventLoopService.addCommand(emptyCommand);

    // Регистрируем handler-ы
    ExceptionHandler.registerHandler(rotateCommandMock, getAngleException, logHandlerMock);

    try {
      lock.lock();
      // Assert (thread "Test worker". Another thread "EventLoop-Thread" is still sleeping.)
      // В этой точке команды(кроме StopCommand) еще не начали выполняться.
      // После выполнения cv.signalAll() "проснется" thread "EventLoop-Thread" и из очереди (в
      // thread-e "EventLoop-Thread")
      // продолжат вытаскиваться и выполняться команды.
      cv.signalAll(); // Call in thread "Test worker".
      logger.debug("<<<<<<<<< Thread 'EventLoop-Thread' woke up <<<<<<<<<");
    } finally {
      lock.unlock();
    }

    // Ждем пока очередь не станет пустой
    waitUntilCommandsCountIsZero();

    // Assert (thread "Test thread")
    logger.debug("Проверяем, что команда {} была вызвана один раз", logCommandSpy);
    verify(logCommandSpy, times(1)).execute();
  }

  @Test
  @DisplayName("Обработчик исключения, который ставит Команду, пишущую в лог в очередь Команд.")
  void handlerShouldAddLogCommandToQueue() {
    // Arrange (thread "Test worker")
    Lock lock = new ReentrantLock();
    // Condition variable
    Condition cv = lock.newCondition();
    // Все команды выполняются в thread-e "EventLoop-thread".

    // StopCommand - команда, которая останавливает(cv.await) thread "EventLoop-thread" в котором
    // работает блокирующая очередь.
    StopCommand stopCommandSpy = spy(new StopCommand());
    // Реализация, которая заменяет логику реального метода(StopCommand#execute).
    doAnswer(
            invocation -> { // Call in thread "EventLoop-thread"
              try {
                lock.lock();
                // После выполнения cv.await() "заснет" thread "EventLoop-Thread" и из очереди(в
                // thread-e "EventLoop-Thread") перестанут вытаскиваться команды.
                logger.debug(
                    "Executing command: {}\n>>>>>>>>> Current thread goes to sleep >>>>>>>>>",
                    stopCommandSpy);
                cv.await(); // Call in thread "EventLoop-Thread"
                return null;
              } finally {
                lock.unlock();
              }
            })
        .when(stopCommandSpy)
        .execute();

    RotateCommand rotateCommandMock = mock(RotateCommand.class);
    // При выполнении метода execute команды rotateCommandMock будет выброшено исключение
    // getAngleException
    doThrow(getAngleException).when(rotateCommandMock).execute();

    // Call in thread "Test worker"
    LogCommand logCommandSpy = spy(new LogCommand(getAngleException));
    AddCommandHandler addCommandHandler = new AddCommandHandler(eventLoopService, logCommandSpy);

    // Добавляем команды в очередь
    eventLoopService.addCommand(stopCommandSpy); // останавливает thread "EventLoop-thread"
    eventLoopService.addCommand(moveCommand);
    eventLoopService.addCommand(rotateCommandMock); // выбрасывает getAngleException
    eventLoopService.addCommand(fireCommand);

    // Регистрируем handler-ы
    ExceptionHandler.registerHandler(rotateCommandMock, getAngleException, addCommandHandler);

    try {
      lock.lock();
      // Assert (thread "Test worker". Another thread "EventLoop-Thread" is still sleeping.)
      // В этой точке команды(кроме StopCommand) еще не начали выполняться.
      // После выполнения cv.signalAll() "проснется" thread "EventLoop-Thread" и из очереди(в
      // thread-e "EventLoop-Thread")
      // продолжат вытаскиваться и выполняться команды.
      cv.signalAll(); // Call in thread "Test worker".
      logger.debug("<<<<<<<<< Thread 'EventLoop-Thread' woke up <<<<<<<<<");
    } finally {
      lock.unlock();
    }

    // Ждем пока очередь не станет пустой
    waitUntilCommandsCountIsZero();

    // Assert (thread "Test thread")
    logger.debug("Проверяем, что команда {} была вызвана один раз", logCommandSpy);
    verify(logCommandSpy, times(1)).execute();
  }

  @Test
  @DisplayName("Команда, которая повторяет Команду, выбросившую исключение")
  void shouldRetryCommandThrowException() {
    // Arrange (thread "Test worker")
    Lock lock = new ReentrantLock();
    // Condition variable
    Condition cv = lock.newCondition();

    // Все команды выполняются в thread-e "EventLoop-thread".
    // StopCommand - команда, которая останавливает(cv.await) thread "EventLoop-thread" в котором
    // работает блокирующая очередь.
    StopCommand stopCommandSpy = spy(new StopCommand());
    // Реализация, которая заменяет логику реального метода(StopCommand#execute).
    doAnswer(
            invocation -> { // Call in thread "EventLoop-thread"
              try {
                lock.lock();
                // После выполнения cv.await() "заснет" thread "EventLoop-Thread" и из очереди(в
                // thread-e "EventLoop-Thread") перестанут вытаскиваться команды.
                logger.debug(
                    "Executing command: {}\n>>>>>>>>> Current thread goes to sleep >>>>>>>>>",
                    stopCommandSpy);
                cv.await(); // Call in thread "EventLoop-Thread"
                return null;
              } finally {
                lock.unlock();
              }
            })
        .when(stopCommandSpy)
        .execute();

    RotateCommand rotateCommandMock = mock(RotateCommand.class);
    // При выполнении метода execute команды rotateCommandMock будет выброшено исключение
    // getAngleException
    doAnswer(
            invocation -> { // Call in thread "EventLoop-thread"
              logger.debug(
                  "Executing command: {} -> throw exception: {}",
                  rotateCommandMock,
                  getAngleException.getClass().getSimpleName());
              throw getAngleException;
            })
        .when(rotateCommandMock)
        .execute();

    RetryCommand retryCommandMock = spy(new RetryCommand(rotateCommandMock));
    // При выполнении метода execute команды retryCommandMock будет повторно выброшено исключение
    // getAngleException
    doAnswer(
            invocation -> { // Call in thread "EventLoop-thread"
              logger.debug(
                  "Executing command: {} -> throw exception: {}",
                  retryCommandMock,
                  getAngleException.getClass().getSimpleName());
              throw getAngleException; // повторитель команды, выбросил исключение
            })
        .when(retryCommandMock)
        .execute();

    RetryHandler retryHandlerMock = mock(RetryHandler.class);
    // Добавляем в очередь команду RetryCommand,
    doAnswer(
            invocation -> { // Call in thread "EventLoop-thread"
              logger.debug(
                  "Executing method apply(), handler: {}\nAdd command: {}, successCommand: {}",
                  retryHandlerMock.getClass().getSimpleName(),
                  retryCommandMock,
                  emptyCommand);
              eventLoopService.addCommand(
                  retryCommandMock); // ставим в очередь Команду - повторитель команды, выбросившей
              // исключение
              return emptyCommand; // возвращаем пустую команду
            })
        .when(retryHandlerMock)
        .apply(any(), any());

    // Call in thread "Test worker"
    // Добавляем команды в очередь
    eventLoopService.addCommand(stopCommandSpy); // останавливает thread "EventLoop-thread"
    eventLoopService.addCommand(moveCommand);
    eventLoopService.addCommand(rotateCommandMock); // выбрасывает getAngleException
    eventLoopService.addCommand(fireCommand);
    eventLoopService.addCommand(emptyCommand);

    // Регистрируем handler-ы
    ExceptionHandler.registerHandler(rotateCommandMock, getAngleException, retryHandlerMock);
    ExceptionHandler.registerHandler(retryCommandMock, getAngleException, logHandler);

    try {
      lock.lock();

      // Assert (thread "Test worker". Another thread "EventLoop-Thread" is still sleeping.)
      // В этой точке команды(кроме StopCommand) еще не начали выполняться.
      // После выполнения cv.signalAll() "проснется" thread "EventLoop-Thread" и из очереди(в
      // thread-e "EventLoop-Thread")
      // продолжат вытаскиваться и выполняться команды.
      cv.signalAll(); // Call in thread "Test worker".
      logger.debug("<<<<<<<<< Thread 'EventLoop-Thread' woke up <<<<<<<<<");
    } finally {
      lock.unlock();
    }

    // Ждем пока очередь не станет пустой
    waitUntilCommandsCountIsZero();

    // Assert (thread "Test thread")
    logger.debug("Проверяем, что команда {} была вызвана один раз", rotateCommandMock);
    verify(rotateCommandMock, times(1)).execute();

    logger.debug("Проверяем, что команда {} была вызвана один раз", retryCommandMock);
    verify(retryCommandMock, times(1)).execute();
  }

  @Test
  @DisplayName(
      "Обработчик исключения, который ставит в очередь Команду - повторитель команды, выбросившей исключение.")
  void shouldAddRetryCommandThatThrowException() {
    // Arrange (thread "Test worker")
    Lock lock = new ReentrantLock();
    // Condition variable
    Condition cv = lock.newCondition();

    // Все команды выполняются в thread-e "EventLoop-thread".
    // StopCommand - команда, которая останавливает(cv.await) thread "EventLoop-thread" в котором
    // работает блокирующая очередь.
    StopCommand stopCommandSpy = spy(new StopCommand());
    // Реализация, которая заменяет логику реального метода(StopCommand#execute).
    doAnswer(
            invocation -> { // Call in thread "EventLoop-thread"
              try {
                lock.lock();
                // После выполнения cv.await() "заснет" thread "EventLoop-Thread" и из очереди(в
                // thread-e "EventLoop-Thread") перестанут вытаскиваться команды.
                logger.debug(
                    "Executing command: {}\n>>>>>>>>> Current thread goes to sleep >>>>>>>>>",
                    stopCommandSpy);
                cv.await(); // Call in thread "EventLoop-Thread"
                return null;
              } finally {
                lock.unlock();
              }
            })
        .when(stopCommandSpy)
        .execute();

    RotateCommand rotateCommandMock = mock(RotateCommand.class);
    // При выполнении метода execute команды rotateCommandMock будет выброшено исключение
    // getAngleException
    doAnswer(
            invocation -> { // Call in thread "EventLoop-thread"
              logger.debug(
                  "Executing command: {} -> throw exception: {}",
                  rotateCommandMock,
                  getAngleException.getClass().getSimpleName());
              throw getAngleException;
            })
        .when(rotateCommandMock)
        .execute();

    RetryCommand retryCommandMock = spy(new RetryCommand(rotateCommandMock));
    // При выполнении метода execute команды retryCommandMock будет выброшено исключение
    // getAngleException
    doAnswer(
            invocation -> { // Call in thread "EventLoop-thread"
              logger.debug(
                  "Executing command: {} -> throw exception: {}",
                  retryCommandMock,
                  getAngleException.getClass().getSimpleName());
              throw getAngleException; // повторитель команды, выбросил исключение
            })
        .when(retryCommandMock)
        .execute();

    RetryHandler retryHandlerMock = mock(RetryHandler.class);
    // Добавляем в очередь команду RetryCommand,
    doAnswer(
            invocation -> { // Call in thread "EventLoop-thread"
              logger.debug(
                  "Executing method apply(), handler: {}\nAdd command: {}, successCommand: {}",
                  retryHandlerMock.getClass().getSimpleName(),
                  retryCommandMock,
                  emptyCommand);
              eventLoopService.addCommand(
                  retryCommandMock); // ставим в очередь Команду - повторитель команды, выбросившей
              // исключение
              return emptyCommand; // возвращаем пустую команду
            })
        .when(retryHandlerMock)
        .apply(any(), any());

    // Call in thread "Test worker"
    // Добавляем команды в очередь
    eventLoopService.addCommand(stopCommandSpy); // останавливает thread "EventLoop-thread"
    eventLoopService.addCommand(moveCommand);
    eventLoopService.addCommand(rotateCommandMock); // выбрасывает getAngleException
    eventLoopService.addCommand(fireCommand);

    // Регистрируем handler-ы
    ExceptionHandler.registerHandler(rotateCommandMock, getAngleException, retryHandlerMock);
    ExceptionHandler.registerHandler(retryCommandMock, getAngleException, logHandler);

    try {
      lock.lock();

      // Assert (thread "Test worker". Another thread "EventLoop-Thread" is still sleeping.)
      // В этой точке команды(кроме StopCommand) еще не начали выполняться.
      // После выполнения cv.signalAll() "проснется" thread "EventLoop-Thread" и из очереди(в
      // thread-e "EventLoop-Thread")
      // продолжат вытаскиваться и выполняться команды.
      cv.signalAll(); // Call in thread "Test worker".
      logger.debug("<<<<<<<<< Thread 'EventLoop-Thread' woke up <<<<<<<<<");
    } finally {
      lock.unlock();
    }

    // Ждем пока очередь не станет пустой
    waitUntilCommandsCountIsZero();

    // Assert (thread "Test thread")
    logger.debug("Проверяем, что команда {} была вызвана один раз", retryCommandMock);
    verify(retryCommandMock, times(1)).execute();
  }

  @Test
  @DisplayName(
      "Обработка исключений - при первом выбросе исключения повторить команду, при повторном выбросе исключения записать информацию в лог")
  void shouldHandleRetryAndLog() {
    // Arrange (thread "Test worker")
    Lock lock = new ReentrantLock();
    // Condition variable
    Condition cv = lock.newCondition();

    // Все команды выполняются в thread-e "EventLoop-thread".
    // StopCommand - команда, которая останавливает(cv.await) thread "EventLoop-thread" в котором
    // работает блокирующая очередь.
    StopCommand stopCommandSpy = spy(new StopCommand());
    // Реализация, которая заменяет логику реального метода(StopCommand#execute).
    doAnswer(
            invocation -> { // Call in thread "EventLoop-thread"
              try {
                lock.lock();
                // После выполнения cv.await() "заснет" thread "EventLoop-Thread" и из очереди(в
                // thread-e "EventLoop-Thread") перестанут вытаскиваться команды.
                logger.debug(
                    "Executing command: {}\n>>>>>>>>> Current thread goes to sleep >>>>>>>>>",
                    stopCommandSpy);
                cv.await(); // Call in thread "EventLoop-Thread"
                return null;
              } finally {
                lock.unlock();
              }
            })
        .when(stopCommandSpy)
        .execute();

    RotateCommand rotateCommandMock = mock(RotateCommand.class);
    // При выполнении метода execute команды rotateCommandMock будет выброшено исключение
    // getAngleException в первый раз
    doAnswer(
            invocation -> { // Call in thread "EventLoop-thread"
              logger.debug(
                  "Executing command: {} -> throw exception: {}",
                  rotateCommandMock,
                  getAngleException.getClass().getSimpleName());
              throw getAngleException;
            })
        .when(rotateCommandMock)
        .execute();

    // При повторном выполнении метода execute команды rotateCommandMock будет выброшено исключение
    // getAngleException во второй раз
    RetryCommand retryCommandSpy = spy(new RetryCommand(rotateCommandMock));
    doAnswer(
            invocation -> { // Call in thread "EventLoop-thread"
              logger.debug(
                  "Executing command: {} -> throw exception: {}",
                  rotateCommandMock,
                  getAngleException.getClass().getSimpleName());
              throw getAngleException;
            })
        .when(retryCommandSpy)
        .execute();

    RetryHandler retryHandlerMock = mock(RetryHandler.class);
    // Добавляем в очередь команду RetryCommand,
    doAnswer(
            invocation -> { // Call in thread "EventLoop-thread"
              logger.debug(
                  "Executing method apply(), handler: {}\nAdd command: {}, retryCommand: {}",
                  retryHandlerMock.getClass().getSimpleName(),
                  retryCommandSpy,
                  emptyCommand);
              eventLoopService.addCommand(
                  retryCommandSpy); // ставим в очередь Команду - повторитель команды, выбросившей
              // исключение
              return emptyCommand; // возвращаем пустую команду
            })
        .when(retryHandlerMock)
        .apply(any(), any());

    // Call in thread "Test worker"
    // Добавляем команды в очередь
    eventLoopService.addCommand(stopCommandSpy); // останавливает thread "EventLoop-thread"
    eventLoopService.addCommand(moveCommand);
    eventLoopService.addCommand(rotateCommandMock); // выбрасывает getAngleException
    eventLoopService.addCommand(retryCommandSpy); // выбрасывает getAngleException
    eventLoopService.addCommand(fireCommand);
    eventLoopService.addCommand(emptyCommand);

    // Регистрируем handler-ы
    ExceptionHandler.registerHandler(
        rotateCommandMock,
        getAngleException,
        retryHandlerMock); // при первом выбросе исключения повторить команду
    ExceptionHandler.registerHandler(
        retryCommandSpy,
        getAngleException,
        logHandler); // при втором выбросе исключения записать в лог

    try {
      lock.lock();

      // Assert (thread "Test worker". Another thread "EventLoop-Thread" is still sleeping.)
      // В этой точке команды(кроме StopCommand) еще не начали выполняться.
      // После выполнения cv.signalAll() "проснется" thread "EventLoop-Thread" и из очереди(в
      // thread-e "EventLoop-Thread")
      // продолжат вытаскиваться и выполняться команды.
      cv.signalAll(); // Call in thread "Test worker".
      logger.debug("<<<<<<<<< Thread 'EventLoop-Thread' woke up <<<<<<<<<");
    } finally {
      lock.unlock();
    }

    // Ждем пока очередь не станет пустой
    waitUntilCommandsCountIsZero();

    // Assert (thread "Test thread")
    logger.debug("Проверяем, что команда {} была вызвана один раз", rotateCommandMock);
    verify(rotateCommandMock, times(1)).execute();

    logger.debug("Проверяем, что команда {} была вызвана два раза", retryCommandSpy);
    verify(retryCommandSpy, times(2)).execute();
  }

  /*-
     Создать новую команду, точно такую же как RetryCommand. Тип этой команды будет показывать, что Команду не удалось выполнить два раза.

     Есть команда, которую вы только что извлекли из очереди команд и она выбросила исключение.
     Пробуем два раза повторить команду, а на третий раз пишем в лог.
     Т.е. исходную команду (например RotateCommand) нужно вложить в другую команду, которая выполняется один раз.
     FirsTimeCommand, исключение вылетит из этой команды.
     FirsTimeCommand вызывает основную команду.
  */
  @Test
  @DisplayName("Обработка исключения - повторить два раза, потом записать в лог")
  void shouldFailTwiceAndLog() {
    // Arrange (thread "Test worker")
    Lock lock = new ReentrantLock();
    // Condition variable
    Condition cv = lock.newCondition();

    // Все команды выполняются в thread-e "EventLoop-thread".
    // StopCommand - команда, которая останавливает(cv.await) thread "EventLoop-thread" в котором
    // работает блокирующая очередь.
    StopCommand stopCommandSpy = spy(new StopCommand());
    // Реализация, которая заменяет логику реального метода(StopCommand#execute).
    doAnswer(
            invocation -> { // Call in thread "EventLoop-thread"
              try {
                lock.lock();
                // После выполнения cv.await() "заснет" thread "EventLoop-Thread" и из очереди(в
                // thread-e "EventLoop-Thread") перестанут вытаскиваться команды.
                logger.debug(
                    "Executing command: {}\n>>>>>>>>> Current thread goes to sleep >>>>>>>>>",
                    stopCommandSpy);
                cv.await(); // Call in thread "EventLoop-Thread"
                return null;
              } finally {
                lock.unlock();
              }
            })
        .when(stopCommandSpy)
        .execute();

    RotateCommand rotateCommandMock = mock(RotateCommand.class);
    // При выполнении команды rotateCommandMock будет выброшено исключение getAngleException
    doAnswer(
            invocation -> { // Call in thread "EventLoop-thread"
              logger.debug(
                  "Executing command: {} -> throw exception: {}",
                  rotateCommandMock,
                  getAngleException.getClass().getSimpleName());
              throw getAngleException;
            })
        .when(rotateCommandMock)
        .execute();

    // Call in thread "Test worker"
    // Добавляем команды в очередь
    eventLoopService.addCommand(stopCommandSpy); // останавливает thread "EventLoop-thread"
    eventLoopService.addCommand(moveCommand);
    eventLoopService.addCommand(rotateCommandMock); // выбрасывает getAngleException
    eventLoopService.addCommand(fireCommand);
    eventLoopService.addCommand(emptyCommand);

    // Регистрируем handler-ы
    DoubleRetryAndLogHandler doubleRetryAndLogHandler =
        new DoubleRetryAndLogHandler(eventLoopService);
    ExceptionHandler.registerHandler(
        rotateCommandMock, getAngleException, doubleRetryAndLogHandler);

    try {
      lock.lock();

      // Assert (thread "Test worker". Another thread "EventLoop-Thread" is still sleeping.)
      // В этой точке команды(кроме StopCommand) еще не начали выполняться.
      // После выполнения cv.signalAll() "проснется" thread "EventLoop-Thread" и из очереди(в
      // thread-e "EventLoop-Thread")
      // продолжат вытаскиваться и выполняться команды.
      cv.signalAll(); // Call in thread "Test worker".
      logger.debug("<<<<<<<<< Thread 'EventLoop-Thread' woke up <<<<<<<<<");
    } finally {
      lock.unlock();
    }

    // Ждем пока очередь не станет пустой
    waitUntilCommandsCountIsZero();

    // Assert (thread "Test thread")
    logger.debug("Проверяем, что команда {} была вызвана три раза", rotateCommandMock);
    // 3 раза = original + FirstTimeCommand + SecondTimeCommand
    verify(rotateCommandMock, times(3)).execute();
  }

  /*-
      Использую внешнюю библиотеку https://github.com/awaitility/awaitility
      This approach is highly recommended for unit and integration testing.
      Why is better to use lib Awaitility:
      - Efficiency: Awaitility polls the condition at regular intervals (e.g., every 100 milliseconds by default) instead of continuously spinning.
        This frees up the CPU.
      - Readability: The code clearly states its intent: "await until the commands count is zero, but not for more than timeout(10 seconds)."
      - Reliability: It includes a timeout, which prevents tests from hanging indefinitely if the condition is never met.
  */
  private void waitUntilCommandsCountIsZero() {
    await()
        .atMost(TIME_OUT_MS, MILLISECONDS)
        .until(
            () -> { // Thread "awaitility-thread"
              logger.debug(
                  "Await until the commands count is zero, but not for more than timeout = {} ms.",
                  TIME_OUT_MS);
              return eventLoopService.getCommandsCount() == 0;
            });
  }
}
