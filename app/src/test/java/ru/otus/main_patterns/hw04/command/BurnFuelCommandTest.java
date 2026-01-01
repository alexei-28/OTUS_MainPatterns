package ru.otus.main_patterns.hw04.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.otus.main_patterns.hw04.Fuel;
import ru.otus.main_patterns.hw04.impl.FuelableImpl;
import ru.otus.main_patterns.hw04.interfaces.Fuelable;

class BurnFuelCommandTest {
  private Fuelable fuelableMock = mock(Fuelable.class);

  @Test
  void setFuelError() {}

  @Test
  @DisplayName("Должно остаться 8 единиц топлива после выполнения команды")
  void shouldBe8UnitsFuelWhenBurnFuelCommandExecute() {
    // Arrange
    Fuel expected = new Fuel(8);
    FuelableImpl fuelable = new FuelableImpl(new Fuel(10), new Fuel(2));
    BurnFuelCommand burnFuelCommand = new BurnFuelCommand(fuelable);

    // Act
    burnFuelCommand.execute();

    // Assert
    Fuel actual = fuelable.getFuel();
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  @DisplayName(
      "Для объекта, имеющего 8 единиц топлива не хватает топлива если израсходовать 10 единиц топлива")
  void shouldBeNegativeUnitsFuelWhenBurnFuelCommandExecute() {
    // Arrange
    Fuel expected = new Fuel(-2);
    FuelableImpl fuelable = new FuelableImpl(new Fuel(8), new Fuel(10));
    BurnFuelCommand burnFuelCommand = new BurnFuelCommand(fuelable);

    // Act
    burnFuelCommand.execute();

    // Assert
    Fuel actual = fuelable.getFuel();
    assertThat(actual).isEqualTo(expected);
  }
}
