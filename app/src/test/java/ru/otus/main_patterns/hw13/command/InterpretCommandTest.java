package ru.otus.main_patterns.hw13.command;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.otus.main_patterns.hw13.ActionRegistry;
import ru.otus.main_patterns.hw13.GameObject;
import ru.otus.main_patterns.hw13.IoC;
import ru.otus.main_patterns.hw13.UObject;
import ru.otus.main_patterns.hw13.exception.AccessDeniedException;
import ru.otus.main_patterns.hw13.handler.*;

class InterpretCommandTest {

  @BeforeEach
  void setUp() {
    IoC.clear();
    ActionRegistry.clear();
  }

  // Happy path — команда выполняется
  @Test
  void shouldExecuteCommandSuccessfully() {

    // arrange
    UObject ship = new GameObject();
    ship.setProperty("id", "1");
    ship.setProperty("ownerId", "player1");

    UObject order = new GameObject();
    order.setProperty("id", "1");
    order.setProperty("action", "StartMove");

    Command command = mock(Command.class);
    ActionHandler handler = mock(ActionHandler.class);

    when(handler.handle(order, ship)).thenReturn(command);

    ActionRegistry.register("StartMove", handler);

    IoC.register("Objects.GetById", args -> ship);

    InterpretCommand interpret = new InterpretCommand(order, "player1");

    // act
    interpret.execute();

    // assert
    verify(handler, times(1)).handle(order, ship);
    verify(command, times(1)).execute();
  }

  // Проверка доступа (owner check)
  @Test
  void shouldThrowWhenPlayerTriesToControlForeignObject() {

    // arrange
    UObject ship = new GameObject();
    ship.setProperty("id", "1");
    ship.setProperty("ownerId", "player1");

    UObject order = new GameObject();
    order.setProperty("id", "1");
    order.setProperty("action", "StartMove");

    IoC.register("Objects.GetById", args -> ship);

    InterpretCommand cmd = new InterpretCommand(order, "enemyPlayer");

    // act / assert
    assertThatThrownBy(cmd::execute)
        .isInstanceOf(AccessDeniedException.class)
        .hasMessageContaining("чужим объектом");
  }

  // Проверка отсутствующего action в registry
  @Test
  void shouldFailWhenActionNotRegistered() {
    // arrange
    UObject ship = new GameObject();
    ship.setProperty("id", "1");
    ship.setProperty("ownerId", "player1");

    UObject order = new GameObject();
    order.setProperty("id", "1");
    order.setProperty("action", "UnknownAction");

    IoC.register("Objects.GetById", args -> ship);

    ActionRegistry.register("StartMove", mock(ActionHandler.class)); // другое действие

    InterpretCommand cmd = new InterpretCommand(order, "player1");

    // act / assert
    assertThatThrownBy(cmd::execute).isInstanceOf(RuntimeException.class);
  }

  // Проверка IoC вызова объекта
  @Test
  void shouldUseIoCToResolveGameObject() {
    // arrange
    UObject ship = new GameObject();
    ship.setProperty("id", "1");
    ship.setProperty("ownerId", "player1");

    UObject order = new GameObject();
    order.setProperty("id", "1");
    order.setProperty("action", "StartMove");

    IoC.register("Objects.GetById", args -> ship);

    ActionHandler handler = mock(ActionHandler.class);
    Command command = mock(Command.class);

    when(handler.handle(order, ship)).thenReturn(command);

    ActionRegistry.register("StartMove", handler);

    InterpretCommand cmd = new InterpretCommand(order, "player1");
    // act
    cmd.execute();

    // assert
    verify(handler).handle(order, ship);
    verify(command).execute();
  }

  // Проверка полного pipeline
  @Test
  void shouldPassThroughFullPipeline() {
    // arrange
    UObject ship = new GameObject();
    ship.setProperty("id", "1");
    ship.setProperty("ownerId", "player1");

    UObject order = new GameObject();
    order.setProperty("id", "1");
    order.setProperty("action", "StartMove");

    Command command = mock(Command.class);
    ActionHandler handler = mock(ActionHandler.class);

    when(handler.handle(order, ship)).thenReturn(command);

    IoC.register("Objects.GetById", args -> ship);
    ActionRegistry.register("StartMove", handler);

    // act
    new InterpretCommand(order, "player1").execute();

    // assert
    verify(handler).handle(order, ship);
    verify(command).execute();
  }
}
