package ru.otus.main_patterns.hw12.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import ru.otus.main_patterns.hw12.GameObject;
import ru.otus.main_patterns.hw12.handler.CollisionCheckHandler;
import ru.otus.main_patterns.hw12.handler.DamageHandler;
import ru.otus.main_patterns.hw12.handler.Handler;

class CollisionCommandTest {

  // Проверка: execute вызывает chain
  @Test
  void shouldCallChainHandle() {
    // Arrange
    GameObject a = new GameObject(1, 0, 0, 5);
    GameObject b = new GameObject(2, 1, 1, 5);
    Handler chain = mock(Handler.class);
    CollisionCommand command = new CollisionCommand(a, b, chain);

    // Act
    command.execute();

    // Assert
    verify(chain, times(1)).handle(a, b);
  }

  // Проверка: передаются правильные объекты
  @Test
  void shouldPassCorrectObjectsToChain() {

    GameObject a = new GameObject(1, 10, 10, 5);
    GameObject b = new GameObject(2, 20, 20, 5);

    Handler chain = mock(Handler.class);

    CollisionCommand command = new CollisionCommand(a, b, chain);

    command.execute();

    verify(chain).handle(eq(a), eq(b));
  }

  // Проверка Chain (интеграционный тест)
  @Test
  void shouldExecuteChainHandlersOnCollision() {

    GameObject a = new GameObject(1, 0, 0, 10);
    GameObject b = new GameObject(2, 5, 5, 10); // есть коллизия

    CollisionCheckHandler h1 = spy(new CollisionCheckHandler());
    DamageHandler h2 = spy(new DamageHandler());

    h1.setNext(h2);

    CollisionCommand command = new CollisionCommand(a, b, h1);

    command.execute();

    verify(h1, times(1)).handle(a, b);
    verify(h2, times(1)).handle(a, b);
  }

  // Проверка: цепочка останавливается
  @Test
  void shouldStopChainWhenNoCollision() {

    GameObject a = new GameObject(1, 0, 0, 5);
    GameObject b = new GameObject(2, 1000, 1000, 5); // нет коллизии

    CollisionCheckHandler h1 = spy(new CollisionCheckHandler());
    DamageHandler h2 = spy(new DamageHandler());

    h1.setNext(h2);

    CollisionCommand command = new CollisionCommand(a, b, h1);

    command.execute();

    verify(h1, times(1)).handle(a, b);
    verify(h2, never()).handle(any(), any());
  }

  // Edge case: null chain
  @Test
  void shouldThrowIfChainIsNull() {

    GameObject a = new GameObject(1, 0, 0, 5);
    GameObject b = new GameObject(2, 1, 1, 5);

    CollisionCommand command = new CollisionCommand(a, b, null);

    assertThatThrownBy(command::execute).isInstanceOf(NullPointerException.class);
  }

  // Проверка: команда не меняет состояние
  @Test
  void shouldNotModifyObjects() {

    GameObject a = new GameObject(1, 0, 0, 5);
    GameObject b = new GameObject(2, 1, 1, 5);

    Handler chain = mock(Handler.class);

    CollisionCommand command = new CollisionCommand(a, b, chain);

    command.execute();

    assertThat(0).isEqualTo(a.getX());
    assertThat(1).isEqualTo(b.getX());
  }
}
