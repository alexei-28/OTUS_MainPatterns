package ru.otus.main_patterns.hw05.core;

import java.util.function.BiFunction;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.main_patterns.hw05.impl.UpdateIocResolveDependencyStrategyCommand;

/**
 * Контейнер инверсии зависимостей(Ioc)- частный случай расширяемой фабрики.
 *
 * <p>Help: https://github.com/etyumentcev/appserver/blob/main/appserver/core/Ioc.cs
 */
public class IoC {
  private static final Logger logger = LoggerFactory.getLogger(IoC.class);

  /**
   * Определение расширяемой фабрики. Фабрика соответствует принципам SOLID. Сама фабрика появилась
   * как реализация принципа инверсии зависимостей(Dependency Inversion Principle) — буква D в
   * аббревиатуре SOLID. Как сделать, чтобы фабрика была расширяема и была устойчива к изменениям
   * требований? Смотри метод IoC.resolve(...). Мы заменяем "new A()" на использование фабрики -
   * метод IoC.resolve(...).
   * <p>
   * Метод Ioc.resolve(...) описывает любой вызов конструктора.  Потому-то что:
   * <p>
   * - вместо имени класса у нас будет строка
   * <p>
   * - все параметры мы переносим в метод IoC.resolve()
   *
   * <p>Таким образом метод IoC.resolve() полностью заменяет создание объекта через "new" и мы решаем главную задачу:
   *
   * <p>Теперь, когда нам нужна ссылка на какой-то объект, мы можем написать код, который не будет
   * меняться, когда мы будем менять какие-то правила, как этот объект будет создаваться. Таким
   * образом "new" будет только при регистрации зависимости. В основном коде (клиентский код)
   * оператора "new" не будет. Таким образом мы не нарушаем один из SOLID принципов, а именно
   * принцип открытости и замкнутости (Open close principe)
   *
   * <p>Разрешение зависимости. Вызов стратегии.
   * Пример использования:
   *
   * <p>Пример:
   *   <pre>
   *    {@code
   *        IoC.resolve("ioc.register", "A", (Object[] args)
   *          -> new RegisterDependencyCommand((String) args[0],(Function<Object[],Object>) args[1]);
   *    }
   * <p>1. Создаем базовый IoC контейнер c уже существующей зависимостью "ioc.register" 2. Фабрика
   * должна, с помощью, метода resolve, вернуть команду (Command) 3. Выполнение команды (метод
   * execute) приводит к регистрации зависимости с именем "A" 4. При разрешении этой зависимости
   * выполнится лямбда функция Function<Object[], Object>
   *
   * <p>По умолчанию у стратегии есть ключ. По этому ключи можно заменить на новую стратегию.
   *
   * @param <T> Ожидаемый тип объекта, получаемого в результате разрешения зависимости. Если
   *     полученный объект невозможно привести в запрашиваемому типу, то выбрасывается исключение
   *     ClassCastException
   * @param dependency Строковое имя разрешаемой зависимости. В реализации контейнера по умолчанию
   *     определена только одна зависимость "update.ioc.resolve.dependency.strategy", которая
   *     позволяет переопределить стратегию разрешения зависимостей по-умолчанию. Это аналог имени
   *     Spring bean-а.
   *     <p>Пример:
   *     <pre>
   *      {@code
   *        @Bean("accountService")
   *        public AccountService getAccountService() {
   *            return new AccountService();
   *        }
   *     }</pre>
   *     , где "accountService" - это имя bean-а. В нашем случае - это имя зависимости. Если нам
   *     понадобится разрешить(resolve) AccountService, то нужно вызвать метод getAccountService() и
   *     вернуть ссылку на тип объекта AccountService. В нашем случае метод getAccountService() -
   *     это стратегия разрешения(resolve) зависимости.
   *     <p>
   * @param args Произвольное количество аргументов, которые получает на вход стратегия разрешения
   *     зависимостей. Для переопределения стратегии разрешения зависимостей по-умолчанию на вход
   *     подается лямбда функция типа Function<BiFunction<String, Object[], Object>,
   *     BiFunction<String, Object[], Object>>, которая на вход принимает текущую стратегию
   *     разрешения зависимостей типа BiFunction<String, Object[], Object>, на выходе возвращает
   *     новую стратегию типа BiFunction<String, Object[], Object>.
   *     <p>Пример:
   *     <pre>{@code
   *        InterfaceA valA = IoC.<InterfaceA>resolve("productA",1,"abc");
   *        InterfaceB valB = IoC.<InterfaceB>resolve("productB","1234567");
   *        InterfaceC valC = IoC.<InterfaceC>resolve("productC");
   *     }</pre>
   *
   * @return Объект(команда), полученный в результате разрешения зависимости. Если указана
   *     несуществующая зависимость, то выбрасывается исключение IllegalArgumentException.
   */
  @SuppressWarnings("unchecked")
  public static <T> T resolve(String dependency, Object... args) {
    return (T) strategy.apply(dependency, args);
  }

  /** Стратегия, которая умеет заменять сама себя. */
  @SuppressWarnings("unchecked")
  public static BiFunction<String, Object[], Object> strategy =
      (String dependency, Object[] args) -> {
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
      };
}
