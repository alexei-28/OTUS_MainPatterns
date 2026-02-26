package ru.otus.main_patterns.hw06.impl;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.otus.main_patterns.hw06.IoC;
import ru.otus.main_patterns.hw06.entity.Vector;
import ru.otus.main_patterns.hw06.generated.impl.MovableImpl;
import ru.otus.main_patterns.hw06.interfaces.Movable;
import ru.otus.main_patterns.hw06.interfaces.UObject;

class MovableImplTest {
  private Class<MovableImpl> clazz;

  @BeforeEach
  void setup() {
    clazz = MovableImpl.class;
  }

  @Test
  void classShouldDirectlyImplementMovable() {
    assertThat(clazz.getInterfaces())
        .as("MovableImpl should directly implement Movable")
        .contains(Movable.class);
  }

  @Test
  void shouldCreateInstanceOfAdapterByIoC() {
    // Arrange
    UObject uObjectMock = mock(UObject.class);

    // Act
    Movable adapter = IoC.<Movable>resolve("Adapter", Movable.class, uObjectMock);

    // Asset
    assertThat(adapter).as("Instance should be created").isNotNull();
    assertThat(adapter).isInstanceOf(Movable.class);
  }

  @Test
  void shouldExistsMethodGetPositionAndReturnVector() throws NoSuchMethodException {
    // Act
    Method method = clazz.getMethod("getPosition");

    // Assert
    // метод существует
    assertThat(method).as("Method getPosition should exist").isNotNull();
    // возвращаемый тип
    assertThat(method.getReturnType()).as("Return type should be Vector").isEqualTo(Vector.class);
    // public
    assertThat(Modifier.isPublic(method.getModifiers())).as("Method should be public").isTrue();
    // без параметров
    assertThat(method.getParameterCount()).as("Method should not have parameters").isZero();
  }

  @Test
  void shouldHaveSetPositionMethod() throws Exception {
    // Act
    Method method = clazz.getMethod("setPosition", Vector.class);

    // Assert
    // метод существует
    assertThat(method).as("Method setPosition(Vector) should exist").isNotNull();
    // возвращаемый тип void
    assertThat(method.getReturnType()).as("setPosition should return void").isEqualTo(void.class);
    // public
    assertThat(Modifier.isPublic(method.getModifiers())).as("Method should be public").isTrue();
    // один параметр типа Vector
    assertThat(method.getParameterTypes())
        .as("setPosition should have one parameter of type Vector")
        .containsExactly(Vector.class);
  }

  @Test
  void shouldHaveGetVelocityMethod() throws Exception {
    // Act
    Method method = clazz.getDeclaredMethod("getVelocity");

    // Assert
    // метод существует
    assertThat(method).as("Method getVelocity should exist").isNotNull();
    // возвращаемый тип
    assertThat(method.getReturnType()).as("Return type should be Vector").isEqualTo(Vector.class);
    // public
    assertThat(Modifier.isPublic(method.getModifiers())).as("Method should be public").isTrue();
    // без параметров
    assertThat(method.getParameterCount()).as("Method should not have parameters").isZero();
  }

  @Test
  void shouldHaveFinishMethod() throws Exception {
    // Act
    Method method = clazz.getMethod("finish");

    // Assert
    // метод существует
    assertThat(method).as("Method finish() should exist").isNotNull();
    // возвращаемый тип void
    assertThat(method.getReturnType()).as("finish should return void").isEqualTo(void.class);
    // public
    assertThat(Modifier.isPublic(method.getModifiers())).as("Method should be public").isTrue();
    // у метода нет параметров
    assertThat(method.getParameterCount()).as("Method should not have parameters").isZero();
  }
}
