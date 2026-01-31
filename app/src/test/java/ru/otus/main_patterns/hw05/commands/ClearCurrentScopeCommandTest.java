package ru.otus.main_patterns.hw05.commands;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.Test;

class ClearCurrentScopeCommandTest {

  @Test
  void shouldClearCurrentScope() {
    // Arrange
    Object testScope = null;
    ClearCurrentScopeCommand clearCommand = new ClearCurrentScopeCommand();
    // Act
    clearCommand.execute();
    // Assert
    assertThat(testScope).isEqualTo(InitCommand.currentScopeThreadLocal.get());
  }
}
