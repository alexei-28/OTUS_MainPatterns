package ru.otus.main_patterns.hw04.command;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.otus.main_patterns.hw02.exception.MoveException;
import ru.otus.main_patterns.hw02.model.Point;
import ru.otus.main_patterns.hw04.interfaces.Rotatable;
import ru.otus.main_patterns.hw04.interfaces.UObject;
import ru.otus.main_patterns.hw04.Fuel;
import ru.otus.main_patterns.hw04.exceptions.NotEnoughFuelException;
import ru.otus.main_patterns.hw04.impl.FuelableImpl;
import ru.otus.main_patterns.hw04.impl.RotateAdapter;
import ru.otus.main_patterns.hw04.impl.UObjectImpl;
import ru.otus.main_patterns.hw04.model.Direction;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static ru.otus.main_patterns.hw04.impl.RotateAdapter.ANGULAR_VELOCITY;
import static ru.otus.main_patterns.hw04.impl.RotateAdapter.DIRECTION;

class MacroCommandTest {
    private final BlockingQueue<Command> blockQueue = new ArrayBlockingQueue<>(10);

    @Test
    void shouldSuccessfullExececutedAllCommands() {
        // Arrange
        FuelableImpl fuelable = new FuelableImpl(new Fuel(10), new Fuel(2));
        blockQueue.add(new CheckFuelCommand(fuelable));
        blockQueue.add(new MoveCommand());
        blockQueue.add(new BurnFuelCommand(fuelable));
        MacroCommand macroCommand = new MacroCommand(blockQueue);

        // Act
        macroCommand.execute();

        // Assert
        assertThat(macroCommand.getBlockQueue()).isEmpty();
    }

    @Test
    void shouldThrowExceptionWhenCheckFuelCommand() {
        // Arrange
        FuelableImpl fuelable = new FuelableImpl(new Fuel(2), new Fuel(10));
        blockQueue.add(new CheckFuelCommand(fuelable));
        blockQueue.add(new MoveCommand());
        blockQueue.add(new BurnFuelCommand(fuelable));
        MacroCommand macroCommand = new MacroCommand(blockQueue);

        // Act and Assert
        assertThatThrownBy(macroCommand::execute)
                .isInstanceOf(CommandException.class);
    }

    @Test
    void shouldThrowExceptionWhenCheckFuelCommandAndQueueSizeIsTwo() {
        // Arrange
        int expectedSize = 2;
        FuelableImpl fuelable = new FuelableImpl(new Fuel(2), new Fuel(10));
        blockQueue.add(new CheckFuelCommand(fuelable));
        blockQueue.add(new MoveCommand());
        blockQueue.add(new BurnFuelCommand(fuelable));
        MacroCommand macroCommand = new MacroCommand(blockQueue);

        // Act
        try {
            macroCommand.execute();
        } catch (Exception ex) {
            // Assert
            assertThat(ex.getCause()).isInstanceOf(NotEnoughFuelException.class);
            int actualSize = macroCommand.getBlockQueue().size();
            assertThat(actualSize).isEqualTo(expectedSize);
        }
    }

    @Test
    @DisplayName("Модификация вектора мгновенной скорости при повороте")
    void shouldSuccessChangeVelocityCommand() {
        // Arrange
        Direction expectedDirection = new Direction(17, 5);
        UObject uObject = new UObjectImpl();
        uObject.setProperty(DIRECTION, new Direction(12, 5));
        uObject.setProperty(ANGULAR_VELOCITY, 5);

        // Act
        Rotatable rotatable = new RotateAdapter(uObject);
        FuelableImpl fuelable = new FuelableImpl(new Fuel(10), new Fuel(2));
        blockQueue.add(new CheckFuelCommand(fuelable));
        blockQueue.add(new RotateCommand());
        blockQueue.add(new MoveCommand());
        blockQueue.add(new ChangeVelocityCommand(rotatable));
        MacroCommand macroCommand = new MacroCommand(blockQueue);

        // Act
        macroCommand.execute();

        // Assert
        Direction actualDirection = rotatable.getDirection();
        assertThat(actualDirection).isEqualTo(expectedDirection);
    }

    @Test
    @DisplayName("Не каждый разворачивающийся объект движется")
    void shouldThrowExceptionWhenMoveCommand() {
        // Arrange
        UObject uObject = new UObjectImpl();
        uObject.setProperty(DIRECTION, new Direction(12, 5));
        uObject.setProperty(ANGULAR_VELOCITY, 5);

        // Act
        Rotatable rotatable = new RotateAdapter(uObject);
        FuelableImpl fuelable = new FuelableImpl(new Fuel(10), new Fuel(2));
        MoveCommand moveCommandMock = mock(MoveCommand.class);
        blockQueue.add(new CheckFuelCommand(fuelable));
        blockQueue.add(new RotateCommand());
        blockQueue.add(moveCommandMock);
        blockQueue.add(new ChangeVelocityCommand(rotatable));
        doThrow(new CommandException())
                .when(moveCommandMock)
                .execute();
        MacroCommand macroCommand = new MacroCommand(blockQueue);

        // Act
        try {
            macroCommand.execute();
        } catch (Exception ex) {
            // Assert
            assertThat(ex.getCause()).isInstanceOf(CommandException.class);
        }
    }
}