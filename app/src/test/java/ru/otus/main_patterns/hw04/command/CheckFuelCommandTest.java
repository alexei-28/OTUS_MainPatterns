package ru.otus.main_patterns.hw04.command;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.otus.main_patterns.hw04.Fuel;
import ru.otus.main_patterns.hw04.exceptions.NotEnoughFuelException;
import ru.otus.main_patterns.hw04.interfaces.Fuelable;

class CheckFuelCommandTest {
  private Fuelable fuelableMock = mock(Fuelable.class);

  @Test
  @DisplayName(
      "Для объекта, имеющего 10 единиц топлива хватает топлива если израсходовать 2 единиц топлива")
  void shouldSuccessCheckFuelCommand() {
    // Arrange
    CheckFuelCommand checkFuelCommandSpy = spy(new CheckFuelCommand(fuelableMock));
    when(fuelableMock.getFuel()).thenReturn(spy(new Fuel(10)));
    when(fuelableMock.getConsumedFuel()).thenReturn(spy(new Fuel(2)));

    // Act
    try {
      // if method "execute" throw NotEnoughFuelException then test fail
      checkFuelCommandSpy.execute();
    } catch (NotEnoughFuelException ex) {
      // Assert
      fail();
    }
  }

  @Test
  @DisplayName(
      "Для объекта, имеющего 8 единиц топлива не хватает топлива если израсходовать 10 единиц топлива")
  void shouldThrowNotEnoughFuelException() {
    // Arrange
    CheckFuelCommand checkFuelCommandSpy = spy(new CheckFuelCommand(fuelableMock));
    when(fuelableMock.getFuel()).thenReturn(spy(new Fuel(8)));
    when(fuelableMock.getConsumedFuel()).thenReturn(spy(new Fuel(10)));

    // Act and Assert
    assertThatThrownBy(checkFuelCommandSpy::execute).isInstanceOf(NotEnoughFuelException.class);
  }
}
