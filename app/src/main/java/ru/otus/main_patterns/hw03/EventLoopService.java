package ru.otus.main_patterns.hw03;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.main_patterns.hw03.command.Command;
import ru.otus.main_patterns.hw03.handler.ExceptionHandler;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/*-
  Help:
  - Архитектура и шаблоны проектирования -> 16. QA-сессия
  - Пишем с нуля Event Loop на блокирующих очередях -> https://vkvideo.ru/video-145052891_456245465

  Дано (пример):
  Нужно добавить в очередь три команды,
  Необходимо добавить (в очередь) Команду#2, Команду#3 таким образом, что Команда#1 еще не запущена.
  Алгоритм заполнения блокирующей очереди (необходимо для понимания как писать многопоточные модульные тесты):
  1. Добавляем замокированную Команду#1 в очередь
  2. Замокированная Команда#1 блокирует поток (внутри метода execute() вызываем метод Condition#await - поток засыпает)
  3. Добавляем Команду#2 в очередь
  4. Добавляем Команду#3 в очередь
  5. Команда#3 разблокирует поток (внутри метода execute() вызываем метод Condition#signal - поток просыпается)
  6. Продолжают выполняться(вытаскиваем из очереди) команды находящиеся в очереди (три команды)

  Queue and stop must be in the separate classes.
  Использую Condition Variable (поддерживается на уровне ОС), чтобы не начинать вытаскивать команды из очереди, пока в очередь не будут добавлены все нужные команды.
  Locks are more flexible way to provide mutual exclusion and synchronization in Java, a powerful alternative of synchronized keyword.
 */
public class EventLoopService {
    private final BlockingQueue<Command> blockingQueue = new ArrayBlockingQueue<>(100);
    private boolean canStop = false;
    private Thread executeThread;

    private static final Logger logger = LoggerFactory.getLogger(EventLoopService.class);

    /*-
         Метод должен выполняться в отдельном thread-e.
         Если в очереди есть команда, то извлекаем.
         Если в очереди ничего нет, то этот thread(EventLoop-Thread) засыпает, до тех пор пока в очереди не появится новая команда.
    */
    public void eventLoop() {
        executeThread = new Thread(() -> {
            logger.debug("================== Start eventLoop ==================");
            while (!canStop) { //  Для остановки EventLoop можно сделать отдельную команду.
                Command command = null;
                try {
                    command = blockingQueue.take(); // If blockingQueue is empty then thread waiting until a new command appears in the queue.
                    logger.debug("\neventLoop_execute_command: {}", command);
                    command.execute();
                    logger.debug("eventLoop_success_executed_command: {} -> take next command", command);
                } catch (Exception ex) {
                    // Если команда выбросила исключение, то оно должно быть обработано только в этой секции.
                    logger.error("eventLoop_catch_unsuccess_executed_command: {}, exception: {} -> finding handler", command, ex.getClass().getSimpleName());
                    // Возвращает и выполняет команду, которая вернет систему в работоспособное состояние.
                    Command successCommand = ExceptionHandler.handle(command, ex); // command - команда, которая выбросила исключение(ex)
                    logger.error("eventLoop_catch_success_handle_find_successCommand: {} -> execute", successCommand);
                    successCommand.execute(); // Важно, чтобы successCommand.execute() не выбрасывала исключение.
                    logger.debug("eventLoop_catch_after_successCommand {} -> Take next command", successCommand);
                }
            }
        });
        executeThread.setName("EventLoop-Thread");
        executeThread.start();
    }

    public void addCommand(Command command) {
        logger.debug("Add command: {}", command);
        blockingQueue.add(command);
    }

    public int getCommandsCount() {
        return blockingQueue.size();
    }

}
