package ru.otus.main_patterns.hw01;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/*-
 Run a Specific Test Class:

    ./gradlew :app:test --tests "ru.otus.main_patterns.hw01.HW01Test"

Run a Specific Test Method within a Class
    ./gradlew :app:test --tests "ru.otus.main_patterns.hw01.HW01Test.should_return_empty_array"

*/

public class HW01Test {
    private HW01 hw01;

    static Stream<double[]> provideDoubleInfinityArrays() {
        return Stream.of(
                new double[]{Double.POSITIVE_INFINITY, 1.0, 2.0},
                new double[]{3.0, Double.POSITIVE_INFINITY, 2.0},
                new double[]{3.0, 1.0, Double.POSITIVE_INFINITY},
                new double[]{Double.NEGATIVE_INFINITY, 1.0, 5.0},
                new double[]{3.0, Double.NEGATIVE_INFINITY, 5.6},
                new double[]{3.0, 1.0, Double.NEGATIVE_INFINITY}
        );
    }

    static Stream<double[]> provideDoubleNanArrays() {
        return Stream.of(
                new double[]{Double.NaN, 1.0, 2.0},
                new double[]{3.0, Double.NaN, 2.0},
                new double[]{3.0, 1.0, Double.NaN}
        );
    }

    @BeforeEach
    public void setUp() {
       hw01 = new HW01();
    }


    @Test
    @DisplayName("Проверяет, что для уравнения x^2+1 = 0 корней нет (возвращается пустой массив)")
    public void should_not_exist_root() {
        // Arrange
        double[] expected = new double[0];
        double a = 1;
        double b = 0;
        double c = 1;

        // Act
        double[] actual = hw01.solve(a, b, c);

        // Assert
       assertThat(actual)
                        .withFailMessage("Expected: %s, but got: %s", Arrays.toString(expected), Arrays.toString(actual))
                        .containsExactly(expected);
    }

    @Test
    @DisplayName("Проверяет, что для уравнения x^2-1 = 0 есть два корня кратности 1 (x1=1, x2=-1)")
    public void should_exist_two_roots() {
        // Arrange
        double[] expected = new double[] {1.0, -1.0};
        double a = 1;
        double b = 0;
        double c = -1;

        // Act
        double[] actual = hw01.solve(a, b, c);

        // Assert
        assertThat(actual)
                .withFailMessage("Expected: %s, but got: %s", Arrays.toString(expected), Arrays.toString(actual))
                .containsExactly(expected);
    }

    @Test
    @DisplayName("Проверяет, что для уравнения x^2+2x+1 = 0 есть один корень кратности 2 (x1 = x2 = -1).")
    public void should_exist_one_root() {
        // Arrange
        double[] expected = new double[] {-1.0};
        double a = 1;
        double b = 2;
        double c = 1;

        // Act
        double[] actual = hw01.solve(a, b, c);

        // Assert
        assertThat(actual)
                .withFailMessage("Expected: %s, but got: %s", Arrays.toString(expected), Arrays.toString(actual))
                .containsExactly(expected);
    }

    @Test
    @DisplayName("Проверяет, что коэффициент 'a' не может быть равен 0.")
    public void should_throw_exception_when_coefficient_a_is_0() {
        // Arrange
        String expected = "Коэффициент 'а' не может быть равен нулю.";
        double a = 0;
        double b = 2;
        double c = 1;

        // Act
        assertThatThrownBy(() -> hw01.solve(a, b, c))
                .isInstanceOf(IllegalArgumentException.class).hasMessage(expected); // Assert
        }

    @Test
    @DisplayName("Проверяет, что коэффициент 'a' не может быть больше чем эпсилон")
    public void should_throw_exception_when_coefficient_a_is_less_then_epsilon() {
        // Arrange
        double a = 0.00000000001;
        double b = 2;
        double c = 1;

        // Act
        assertThatThrownBy(() -> hw01.solve(a, b, c))
                .isInstanceOf(IllegalArgumentException.class); // Assert
    }

    @ParameterizedTest(name = "Array {0}")
    @MethodSource("provideDoubleNanArrays")
    @DisplayName("Проверяет, что любой коэффициент не может быть равен NaN")
    void should_throw_exception_when_any_coefficient_is_Nan(double[] array) {
        // Arrange
        String expected = "Коэффициент не может быть равен NaN.";
        // Act
        assertThatThrownBy(() -> hw01.solve(array[0], array[1], array[2]))
                .isInstanceOf(IllegalArgumentException.class).hasMessage(expected); // Assert
    }


    @ParameterizedTest(name = "Array {0}")
    @MethodSource("provideDoubleInfinityArrays")
    @DisplayName("Проверяет, что любой коэффициент не может быть равен бесконечности")
    void should_throw_exception_when_any_coefficient_is_infinite(double[] array) {
        // Arrange
        String expected = "Коэффициент не может быть равен бесконечности.";
        // Act
        assertThatThrownBy(() -> hw01.solve(array[0], array[1], array[2]))
                .isInstanceOf(IllegalArgumentException.class).hasMessage(expected); // Assert
    }

}