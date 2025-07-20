package ru.otus.main_patterns.hw02.command;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.otus.main_patterns.UObjectImpl;
import ru.otus.main_patterns.hw02.adapter.MoveAdapter;
import ru.otus.main_patterns.hw02.exception.MoveException;
import ru.otus.main_patterns.hw02.exception.PointNotFoundException;
import ru.otus.main_patterns.hw02.exception.VelocityNotFoundException;
import ru.otus.main_patterns.hw02.inter.Movable;
import ru.otus.main_patterns.hw02.inter.UObject;
import ru.otus.main_patterns.hw02.model.Point;
import ru.otus.main_patterns.hw02.model.Velocity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static ru.otus.main_patterns.hw02.adapter.MoveAdapter.VELOCITY;

/*-
      Прямолинейное движение объекта (параллельный перенос).
      Положение точки#1 = (x,y)
      Вектор скорости = (dx, dy)
      Положение точки#2 = (x+dx, y+dy)

      Т.е. объект может быть представлен двумя полями: Location, Velocity
      Как можно описать вектор скорости (Velocity)?
      Есть модуль вектора = численное значение скорости. Например 72 km/h = длина вектора.
      У вектора еще есть направление.

      Как вычислить координаты вектора?
      Имеем угол a (угол поворота)
      То,
       координата вектора по оси x = V * cos(a)
       координата вектора по оси y = V * sin(a)

      Пример:
      Положение точки#1 = (12,5)
      Вектор скорости = (-7, 3)
      Положение точки#2 = (5, 8)
    */
@ExtendWith(MockitoExtension.class)
class MoveTest {
    private static final String POINT = "point";
    private UObject uObject;
    @InjectMocks
    private Move moveMock;
    @Mock
    private Movable movableMock;
    @Mock
    private Point pointMock;
    @Mock
    private Velocity velocityMock;

    @BeforeEach
    void setUp() {
        uObject = new UObjectImpl();
    }

    @Test
    @DisplayName("Для объекта, находящегося в точке (12, 5) и движущегося со скоростью (-7, 3) движение меняет положение объекта на (5, 8)")
    void shouldMove() {
        // Arrange
        uObject.setProperty(POINT, new Point(12,5));
        uObject.setProperty(VELOCITY, new Velocity(-7,3));

        // Act
        Movable movable = new MoveAdapter(uObject);
        Move move = new Move(movable);
        move.execute();

        // Assert
        Point expectedLocation = new Point(5,8);
        Point actualLocation = movable.getLocation();
        assertThat(actualLocation).isEqualTo(expectedLocation);
    }

    @Test
    @DisplayName("Попытка сдвинуть объект, у которого невозможно прочитать положение в пространстве, приводит к ошибке")
    void shouldThrowExceptionWhenNoFoundPropertyPoint() {
        // Arrange
        when(movableMock.getLocation())
                .thenThrow(new PointNotFoundException());

        // Act and Assert
        assertThatThrownBy(moveMock::execute)
                .isInstanceOf(PointNotFoundException.class);
    }

    @Test
    @DisplayName("Попытка сдвинуть объект, у которого невозможно прочитать значение мгновенной скорости, приводит к ошибке")
    void shouldThrowExceptionWhenNoFoundPropertyVelocity() {
        // Arrange
        when(movableMock.getVelocity())
                .thenThrow(new VelocityNotFoundException());

        // Act and Assert
        assertThatThrownBy(moveMock::execute)
                .isInstanceOf(VelocityNotFoundException.class);
    }

    @Test
    @DisplayName("Попытка сдвинуть объект, у которого невозможно изменить положение в пространстве, приводит к ошибке")
    void shouldThrowExceptionWhenMove() {
        // Arrange
        when(movableMock.getLocation()).thenReturn(pointMock);
        when(movableMock.getVelocity()).thenReturn(velocityMock);
        when(pointMock.plus(any())).thenReturn(pointMock);
        Move move = new Move(movableMock);
        doThrow(new MoveException())
                .when(movableMock)
                .setLocation(any(Point.class));

        // Act and Assert
        assertThatThrownBy(move::execute)
                .isInstanceOf(MoveException.class);
    }

}