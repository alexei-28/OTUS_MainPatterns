package ru.otus.main_patterns.hw05.core;

import java.util.function.BiFunction;
import java.util.function.Function;
import ru.otus.main_patterns.hw05.commands.UpdateIocResolveDependencyStrategyCommand;

/**
 * Контейнер инверсии зависимостей (IoC) — частный случай расширяемой фабрики.
 *
 * <p><a href="https://github.com/etyumentcev/appserver/blob/main/appserver/core/Ioc.cs">Example of
 * IoC (C# reference)</a>
 *
 * <p>Стратегию разрешения зависимостей можно представить в виде функционального интерфейса
 * BiFunction:
 *
 * <p>{@code BiFunction<String, Object[], Object> strategy = (String dependency, Object[] args) ->
 * {} }
 *
 * <p>Определение расширяемой фабрики. Фабрика соответствует принципам SOLID. Сама фабрика появилась
 * как реализация принципа инверсии зависимостей (Dependency Inversion Principle) — буква D в
 * аббревиатуре SOLID.
 *
 * <p>Как сделать, чтобы фабрика была расширяема и была устойчива к изменениям требований? Мы
 * заменяем прямой вызов {@code new A()} на использование фабрики — метод {@code IoC.resolve(...)}.
 *
 * <p>Метод {@code IoC.resolve(...)} описывает любой вызов конструктора (через ключевое слово
 * "new"), потому что:
 *
 * <ul>
 *   <li>вместо имени класса используется строка (имя зависимости);
 *   <li>все параметры конструктора переносятся в аргументы метода {@code resolve()}.
 * </ul>
 *
 * <p>Таким образом, метод {@code IoC.resolve()} полностью заменяет создание объекта через {@code
 * new}, решая главную задачу: клиентский код не меняется при изменении правил создания объектов.
 * Ключевое слово {@code new} будет использоваться только при регистрации зависимости, что позволяет
 * соблюдать принцип открытости/замкнутости (Open-Closed Principle).
 *
 * <p>@param <T> Ожидаемый тип объекта, получаемого в результате разрешения зависимости. Если *
 * полученный объект невозможно привести к запрашиваемому типу, выбрасывается исключение {@link *
 * ClassCastException}. * @param dependency Строковое имя разрешаемой зависимости. Аналог имени
 * Spring bean-а. По умолчанию * определена только одна зависимость: *
 *
 * <p>{@code "update.ioc.resolve.dependency.strategy"}, которая позволяет переопределить глобальную
 * стратегию разрешения зависимостей. *
 *
 * <p><b>Сравнение со Spring:</b> *
 *
 * <pre>{@code
 * @Bean("accountService")
 * public AccountService getAccountService() {
 *      return new AccountService();
 * }
 * }</pre>
 *
 * Здесь {@code "accountService"} — имя зависимости, а метод {@code getAccountService()} — *
 * стратегия разрешения этой зависимости. * @param args Произвольное количество аргументов для
 * стратегии разрешения.
 *
 * <p>@return Объект (или команда), полученный в результате разрешения зависимости.
 *
 * <p>@throws IllegalArgumentException если указана несуществующая зависимость.
 *
 * <p><b>Пример регистрации зависимости "accountService":</b>
 *
 * <pre>{@code
 * Command command = IoC.<Command>resolve("ioc.register", "accountService", (Object[] args) ->
 *      new RegisterDependencyCommand((String) args[0], (Function<Object[], Object>) args[1])).execute();
 * }</pre>
 *
 * <ol>
 *   <li>Используем базовый IoC контейнер с существующей зависимостью {@code "ioc.register"}.
 *   <li>Метод {@code resolve} возвращает команду).
 *   <li>Выполнение команды (метод {@code execute()}) регистрирует зависимость {@code
 *       "accountService"}.
 *   <li>При разрешении этой зависимости выполнится переданная лямбда-функция.
 * </ol>
 */
public class IoC {

  @SuppressWarnings("unchecked")
  public static <T> T resolve(String dependency, Object... args) {
    return (T) strategy.apply(dependency, args);
  }

  /**
   * Дефолтная стратегия, которая умеет заменять сама себя. При переопределении стратегии (через
   * {@code "update.ioc.resolve.dependency.strategy"}) ожидается лямбда-функция типа:
   *
   * <pre>{@code
   * Function<BiFunction<String, Object[], Object>, BiFunction<String, Object[], Object>>
   * }</pre>
   *
   * <p>Она принимает текущую стратегию и возвращает новую.
   *
   * <p><b>Примеры использования:</b>
   *
   * <pre>{@code
   * InterfaceA valA = IoC.<InterfaceA>resolve("productA", 1, "abc");
   * InterfaceB valB = IoC.<InterfaceB>resolve("productB", "1234567");
   * InterfaceC valC = IoC.<InterfaceC>resolve("productC");
   * Command command = IoC.<Command>resolve("Движение по прямой");
   *
   * }</pre>
   */
  @SuppressWarnings("unchecked")
  public static BiFunction<String, Object[], Object> strategy =
      new BiFunction<String, Object[], Object>() {
        @Override
        public Object apply(String dependency, Object[] args) {
          if ("update.ioc.resolve.dependency.strategy".equals(dependency)) {
            Function<BiFunction<String, Object[], Object>, BiFunction<String, Object[], Object>>
                updateIoCStrategy =
                    (Function<
                            BiFunction<String, Object[], Object>,
                            BiFunction<String, Object[], Object>>)
                        args[0];
            return new UpdateIocResolveDependencyStrategyCommand(updateIoCStrategy);
          } else {
            throw new IllegalArgumentException(
                String.format("Dependency %s is not found.", dependency));
          }
        }
      };
}
