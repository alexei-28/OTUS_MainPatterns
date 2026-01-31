package ru.otus.main_patterns.hw05.commands;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SetCurrentScopeCommandTest {

  @AfterEach
  void tearDown() {
    // Очищаем ThreadLocal после каждого теста, чтобы не влиять на другие тесты
    InitCommand.currentScopeThreadLocal.remove();
  }

  @Test
  @DisplayName("Should set scope in the current thread")
  void shouldSetScopeInCurrentThread() {
    // Arrange
    Map<String, Object> scope = new HashMap<>();
    scope.put("key", "value");
    SetCurrentScopeCommand command = new SetCurrentScopeCommand(scope);

    // Act
    command.execute();

    // Assert
    assertThat(InitCommand.currentScopeThreadLocal.get())
        .as("The scope in ThreadLocal should match the one passed to the command")
        .isSameAs(scope);
  }

  @Test
  @DisplayName("Should maintain thread isolation for scopes")
  void shouldMaintainThreadIsolation() throws InterruptedException {
    // Arrange
    Object scopeForThreadA = new Object();
    Object scopeForThreadB = new Object();
    AtomicReference<Object> capturedScopeA = new AtomicReference<>();
    AtomicReference<Object> capturedScopeB = new AtomicReference<>();
    CountDownLatch latch = new CountDownLatch(2);

    // Act
    Thread threadA =
        new Thread(
            () -> {
              new SetCurrentScopeCommand(scopeForThreadA).execute();
              capturedScopeA.set(InitCommand.currentScopeThreadLocal.get());
              latch.countDown();
            });

    Thread threadB =
        new Thread(
            () -> {
              new SetCurrentScopeCommand(scopeForThreadB).execute();
              capturedScopeB.set(InitCommand.currentScopeThreadLocal.get());
              latch.countDown();
            });

    threadA.start();
    threadB.start();
    boolean finished = latch.await(2, TimeUnit.SECONDS);

    // Assert
    assertThat(finished).isTrue();
    assertThat(capturedScopeA.get())
        .as("Thread A should have its own scope")
        .isSameAs(scopeForThreadA)
        .isNotSameAs(capturedScopeB.get());
    assertThat(capturedScopeB.get())
        .as("Thread B should have its own scope")
        .isSameAs(scopeForThreadB);
  }
}
