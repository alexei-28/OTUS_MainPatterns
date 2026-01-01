package ru.otus.main_patterns.hw02.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.otus.main_patterns.UObjectImpl;
import ru.otus.main_patterns.hw02.adapter.RotateAdapter;
import ru.otus.main_patterns.hw02.exception.AngularVelocityNotFoundException;
import ru.otus.main_patterns.hw02.exception.DirectionNotFound;
import ru.otus.main_patterns.hw02.inter.Rotatable;
import ru.otus.main_patterns.hw02.inter.UObject;
import ru.otus.main_patterns.hw02.model.Direction;

class RotateTest {
  public static final String DIRECTION = "Direction";
  public static final String ANGULAR_VELOCITY = "AngularVelocity";
  private UObject uObject;
  private Rotate rotateMock;
  private Rotatable rotatableMock;

  @BeforeEach
  void setUp() {
    uObject = new UObjectImpl();
    rotatableMock = mock(Rotatable.class);
    rotateMock = new Rotate(rotatableMock);
  }

  @Test
  @DisplayName(
      "Для объекта, находящегося в точке (12, 5) и поворачивающего с угловой скоростью 5 меняет положение объекта на (15, 5)")
  void shouldRotate() {
    // Arrange
    uObject.setProperty(DIRECTION, new Direction(12, 5));
    uObject.setProperty(ANGULAR_VELOCITY, 5);

    // Act
    Rotatable rotatable = new RotateAdapter(uObject);
    Rotate rotate = new Rotate(rotatable);
    rotate.execute();

    // Assert
    Direction expectedDirection = new Direction(17, 5);
    Direction actualDirection = rotatable.getDirection();
    assertThat(actualDirection).isEqualTo(expectedDirection);
  }

  @Test
  @DisplayName(
      "Попытка повернуть объект, у которого невозможно прочитать положение в пространстве, приводит к ошибке")
  void shouldThrowExceptionWhenNoFoundDirection() {
    // Arrange
    when(rotatableMock.getDirection()).thenThrow(new DirectionNotFound());

    // Act and Assert
    assertThatThrownBy(rotateMock::execute).isInstanceOf(DirectionNotFound.class);
  }

  @Test
  @DisplayName(
      "Попытка сдвинуть объект, у которого невозможно прочитать значение угловой скорости, приводит к ошибке")
  void shouldThrowExceptionWhenNoFoundAngularVelocity() {
    // Arrange
    when(rotatableMock.getAngularVelocity()).thenThrow(new AngularVelocityNotFoundException());

    // Act and Assert
    assertThatThrownBy(rotateMock::execute).isInstanceOf(AngularVelocityNotFoundException.class);
  }
}
