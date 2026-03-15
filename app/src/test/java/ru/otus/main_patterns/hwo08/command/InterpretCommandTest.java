package ru.otus.main_patterns.hwo08.command;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import ru.otus.main_patterns.hwo08.IoC;
import ru.otus.main_patterns.hwo08.dto.Order;
import ru.otus.main_patterns.hwo08.service.QueueService;

class InterpretCommandTest {
  private static String jsonTemplate;
  private static Order order;
  private static final ObjectMapper objectMapper = new ObjectMapper();

  @BeforeAll
  static void setup() throws IOException {
    jsonTemplate = readResourceFile("order_move.json");
  }

  @BeforeEach
  void setUp() throws JsonProcessingException {
    order = objectMapper.readValue(jsonTemplate, Order.class);
    order.setId(UUID.randomUUID().toString());
  }

  private static String readResourceFile(String fileName) throws IOException {
    return new String(
        Files.readAllBytes(
            Paths.get(InterpretCommandTest.class.getClassLoader().getResource(fileName).getPath())),
        StandardCharsets.UTF_8);
  }

  @Test
  @DisplayName("Создание команды MoveCommand")
  void shouldCreateMoveCommandByIoc() {
    // Arrange
    new InterpretCommand(order);

    // Act
    Command command = IoC.<Command>resolve("create.command", order);

    // Assert
    assertThat(command)
        .as("Команда должна быть создана и быть MoveCommand")
        .isNotNull()
        .isInstanceOf(MoveCommand.class);
  }

  @Test
  @DisplayName("Запрет на создание команды")
  void shouldThrowExceptionWhenCreateCommandIsForbidden() {
    // Arrange
    order.setGameId("game-2");
    order.setPlayerId("player-3");
    InterpretCommand interpretCommand = new InterpretCommand(order);

    // Act and Assert
    assertThatThrownBy(interpretCommand::execute)
        .isInstanceOf(SecurityException.class)
        .hasMessageContaining("is not allowed to execute operation");
  }

  @Test
  @DisplayName("Добавляем команду MoveCommand в очередь без запуска сервера")
  void shouldAddMoveCommandToQueueWhenInterpretCommandExecutes() throws Exception {
    try (MockedConstruction<StartCommand> mocked = mockConstruction(StartCommand.class)) {
      // Arrange
      QueueService queueService = QueueService.getInstance();
      // Очищаем очередь через рефлексию
      Field field = QueueService.class.getDeclaredField("blockingQueue");
      field.setAccessible(true);
      BlockingQueue<Command> queue = (BlockingQueue<Command>) field.get(queueService);
      queue.clear();

      // Act
      InterpretCommand interpretCommand = new InterpretCommand(order);
      interpretCommand.execute();

      // Assert
      assertThat(queue)
          .as("Очередь должна содержать ровно одну команду " + MoveCommand.class.getSimpleName())
          .hasSize(1)
          .anyMatch(
              cmd ->
                  cmd.getClass().getSimpleName().contains(MoveCommand.class.getSimpleName())
                      || cmd.equals(mocked));
    }
  }

  @Test
  @DisplayName("MoveCommand выполняется через InterpretCommand")
  void shouldExecuteMoveCommand() {
    // Arrange
    Map<String, Object> operationArgs = Collections.singletonMap("initialVelocity", 10);
    MoveCommand moveCommandSpy = spy(new MoveCommand(operationArgs));

    try (MockedConstruction<QueueCommand> mockedQueueCommand =
        // Используем MockedConstruction для мокирования конструктора QueueCommand
        mockConstruction(
            QueueCommand.class,
            (mock, context) ->
                doAnswer(
                        invocation -> {
                          // Перехватываем execute() и вызываем реальный MoveCommand.
                          moveCommandSpy.execute();
                          return null;
                        })
                    .when(mock)
                    .execute())) {

      // Act
      InterpretCommand interpretCommand = new InterpretCommand(order);
      interpretCommand.execute();

      // Assert
      // Проверяем, что execute() вызван ровно один раз.
      verify(moveCommandSpy, times(1)).execute();
    }
  }
}
