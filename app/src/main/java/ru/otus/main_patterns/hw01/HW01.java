package ru.otus.main_patterns.hw01;

/*-
   Домашнее задание:
   Разработать набор модульных тестов.

   Необходимо реализовать операцию нахождения квадратного уравнения.
   Предположим, что эта операция описывается следующей функцией c поправкой на конкретный язык программирования.
   В ООП языках эта функция реализуется в виде метода класса.

   solve(double a, double b, double c): double[]

   здесь a, b, c - коэффициенты квадратного уравнения, функция возвращает список корней квадратного уравнения.

   Описание подхода к решению квадратных уравнений.
   Решение квадратного уравнения с помощью дискриминанта.
   Квадратное уравнение имеет вид: ax^2 + bx + c = 0
   Дискриминант D = b^2 - 4ac.

   Если D > 0, то уравнение имеет два различных корня.
   Корни уравнения вычисляются по формулам:
   x1 = (-b + sqrt(D)) / (2a)
   x2 = (-b - sqrt(D)) / (2a)

   Если D = 0, то уравнение имеет один корень (дважды).
   Корень уравнения вычисляется по формуле:
   x = -b / (2a)

   Если D < 0, то уравнение не имеет действительных корней.

   Пример:
   для уравнения x^2 + 5x + 6 = 0
   a = 1, b = 5, c = 6
   Дискриминант D = 25 - 24 = 1 > 0. Значит имеем два корня.
   Корни уравнения:
   x1 = (-5 + sqrt(1)) / (2 * 1) = (-5 + 1) / 2 = -4 / 2 = -2
   x2 = (-5 - sqrt(1)) / (2 * 1) = (-5 - 1) / 2 = -6 / 2 = -3

   Ответ: x1 = -2, x2 = -3
   -2^2 + 5 * -2 + 6 = 0
   -3^2 + 5 * -3 + 6 = 0

*/
public class HW01 {
  public static final double EPSILON = 1e-7; // Приемлемое значение для проверки на ноль

  public double[] solve(Double a, double b, double c) {
    if (isEqualToZero(a)) {
      throw new IllegalArgumentException("Коэффициент 'а' не может быть равен нулю.");
    }
    if (Double.isNaN(a) || Double.isNaN(b) || Double.isNaN(c)) {
      throw new IllegalArgumentException("Коэффициент не может быть равен NaN.");
    }

    if (Double.isInfinite(a) || Double.isInfinite(b) || Double.isInfinite(c)) {
      throw new IllegalArgumentException("Коэффициент не может быть равен бесконечности.");
    }

    double discriminant = b * b - 4 * a * c;
    if (discriminant < 0) {
      return new double[0]; // No real roots
    } else if (isEqualToZero(discriminant)) {
      double root = -b / (2 * a);
      return new double[] {root}; // One real root
    } else {
      double sqrtD = Math.sqrt(discriminant);
      double root1 = (-b + sqrtD) / (2 * a);
      double root2 = (-b - sqrtD) / (2 * a);
      return new double[] {root1, root2}; // Two real roots
    }
  }

  private boolean isEqualToZero(double number) {
    return Math.abs(number) <= EPSILON; // Приемлемое значение на равенство 0
  }
}
