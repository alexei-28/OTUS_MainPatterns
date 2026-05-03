package ru.otus.main_patterns.hw12.command;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import ru.otus.main_patterns.hw12.interfaces.Command;

class MacroCommandTest {
  private MacroCommand macroCommand;
  private Command command1;
  private Command command2;

  @BeforeEach
  void setUp() {
    macroCommand = new MacroCommand();
    command1 = mock(Command.class);
    command2 = mock(Command.class);
  }

  @Test
  void execute_shouldCallAllCommands() {
    // Arrange
    macroCommand.add(command1);
    macroCommand.add(command2);

    // Act
    macroCommand.execute();

    // Assert
    verify(command1, times(1)).execute();
    verify(command2, times(1)).execute();
  }

  @Test
  void execute_shouldDoNothing_whenNoCommands() {
    // Act
    macroCommand.execute();

    // Assert
    // просто проверяем, что ничего не упало
    // (взаимодействий нет)
    verifyNoInteractions(command1, command2);
  }

  @Test
  void clear_shouldRemoveAllCommands() {
    // Arrange
    macroCommand.add(command1);
    macroCommand.add(command2);

    // Act
    macroCommand.clear();
    macroCommand.execute();

    // Assert
    verifyNoInteractions(command1, command2);
  }

  @Test
  void add_shouldAllowDuplicateCommands() {
    // Arrange
    macroCommand.add(command1);
    macroCommand.add(command1);

    // Act
    macroCommand.execute();

    // Assert
    verify(command1, times(2)).execute();
  }

  @Test
  void execute_shouldStopOnFirstException() {
    // Arrange
    MacroCommand macro = new MacroCommand();
    Command ok = mock(Command.class);
    Command fail = mock(Command.class);
    Command never = mock(Command.class);
    doThrow(new RuntimeException("boom")).when(fail).execute();

    // Act
    macro.add(ok);
    macro.add(fail);
    macro.add(never);

    // Assert
    assertThrows(RuntimeException.class, macro::execute);
    verify(ok).execute();
    verify(fail).execute();
    verify(never, never()).execute(); // важно!
  }

  @Test
  void execute_shouldPreserveOrder() {
    // Arrange
    MacroCommand macro = new MacroCommand();
    Command c1 = mock(Command.class);
    Command c2 = mock(Command.class);
    macro.add(c1);
    macro.add(c2);

    // Act
    macro.execute();
    InOrder inOrder = inOrder(c1, c2);

    // Assert
    inOrder.verify(c1).execute();
    inOrder.verify(c2).execute();
  }

  @Test
  void clear_thenAdd_shouldWorkCorrectly() {
    // Arrange
    MacroCommand macro = new MacroCommand();
    Command c = mock(Command.class);
    macro.add(c);
    macro.clear();
    macro.add(c);

    // Act
    macro.execute();

    // Assert
    verify(c, times(1)).execute();
  }

  @Test
  void execute_calledTwice_shouldExecuteTwice() {
    // Arrange
    MacroCommand macro = new MacroCommand();
    Command c = mock(Command.class);
    macro.add(c);

    // Act
    macro.execute();
    macro.execute();

    // Assert
    verify(c, times(2)).execute();
  }
}
