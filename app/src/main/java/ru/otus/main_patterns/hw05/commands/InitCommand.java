package ru.otus.main_patterns.hw05.commands;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.main_patterns.hw05.core.IoC;
import ru.otus.main_patterns.hw05.interfaces.Command;
import ru.otus.main_patterns.hw05.interfaces.DependencyResolver;
import ru.otus.main_patterns.hw05.scopes.DependencyResolverImpl;

/**
 * <a
 * href="https://github.com/etyumentcev/appserver/blob/main/appserver/scopes/InitCommand.cs">Example</a>
 *
 * <p>Scope(context) - это разные области видимости.
 *
 * <p>ThreadLocal — это специальный класс в Java, который позволяет создавать переменные, доступные
 * только для одного конкретного потока. Если обычная статическая переменная видна всем потокам
 * одинаково, то в случае с ThreadLocal у каждого потока будет своя собственная, изолированная копия
 * этой переменной. Изменение значения в одном потоке никак не повлияет на значение в другом.
 *
 * <p>ThreadLocal - это карта, где в качестве ключа выступает идентификатор текущего потока(Thread).
 * Можно по текущему потоку хранить какой-то тип данных.
 *
 * <p>В результате в каждый поток можно положить собственный scope(context). В итоге, когда мы будем
 * обращаться к этой стратегии(через метод IoC.resolve()), то мы сначала идем в ThreadLocal, смотрим
 * в текущем потоке какой текущий контекст(scope) и уже работаем с ним. В итоге получается, что в
 * каждом потоке могут быть как одинаковые, так и разные контексты. Т.е. Получаем, что благодаря
 * ThreadLocal, в одном потоке один контекст, а в другом потоке другой контекст. Т.е. Когда мы
 * выполняем метод IoC.resolve(), первое, что происходит - это мы пойдем в ThreadLocal и получим
 * текущий контекст. В каком бы потоке мы не вызывали мы всегда получим ссылку на тот контекст,
 * который актуален для этого потока. Таким образом мы получаем потокобезопасные реализации.
 * Стратегию проще всего представлять в виде лямбды функции - Function <Object[], Object>.
 *
 * <p>Инициализация многопоточного контейнера. Выполняется только один раз. Эта команда
 * инициализирует IoC контейнер для работы с несколькими потоками. Scope = Map<String,
 * Function<Object[], Object>>
 *
 * <p>При переопределении стратегии (через {@code "update.ioc.resolve.dependency.strategy"})
 * ожидается лямбда-функция типа:
 *
 * <pre>{@code
 * Function<BiFunction<String, Object[], Object>, BiFunction<String, Object[], Object>>
 * }</pre>
 */
public class InitCommand implements Command {
  public static final ThreadLocal<Object> currentScopeThreadLocal =
      ThreadLocal.withInitial(() -> null);
  // key = dependency name, value = strategy as lambda Function<Object[], Object>
  private static final ConcurrentHashMap<String, Function<Object[], Object>> scopesMap =
      new ConcurrentHashMap<>();
  private static final AtomicBoolean isAlreadyExecutesSuccessfully = new AtomicBoolean(false);

  private static final Logger logger = LoggerFactory.getLogger(InitCommand.class);

  /** - Выполняется один раз. */
  @Override
  public void execute() {
    logger.info("execute, isAlreadyExecutesSuccessfully: {}", isAlreadyExecutesSuccessfully);
    if (isAlreadyExecutesSuccessfully.get()) {
      return;
    }

    // Root scope - главный контекст, в который мы складываем разные зависимости
    synchronized (scopesMap) {
      /*-
         SetCurrentScopeCommand - команда установит, в этом потоке, текущий контекст(смена  контекста),
         который будет передан в качестве аргумента(args[0]).
         E.g. change scope:
          IoC.resolve<Command>("ioc.scope.current.set", otherScope).execute()
      */
      scopesMap.putIfAbsent(
          "ioc.scope.current.set", (Object[] args) -> new SetCurrentScopeCommand(args[0]));
      scopesMap.putIfAbsent(
          "ioc.scope.current.clear", (Object[] args) -> new ClearCurrentScopeCommand());

      /*-
        "ioc.scope.current" - возвращает текущий scope, который есть в текущем потоке.
        currentScopeThreadLocal.get() - возвращает значение из текущего потока.
      */
      scopesMap.putIfAbsent(
          "ioc.scope.current",
          (Object[] args) ->
              currentScopeThreadLocal.get() != null ? currentScopeThreadLocal.get() : scopesMap);
      /*-
        У контекста можно прочитать родительский контекст. Все контексты упорядочены иерархически.
        "ioc.scope.parent" - возвращает ссылку на родительский контекст.
      */
      scopesMap.putIfAbsent(
          "ioc.scope.parent",
          (Object[] args) -> {
            throw new RuntimeException("The root scope has no a parent scope.");
          });
      scopesMap.putIfAbsent(
          "ioc.scope.create.empty",
          (Object[] args) -> new HashMap<String, Function<Object[], Object>>());
      scopesMap.putIfAbsent(
          "ioc.scope.create",
          (Object[] args) -> {
            Map<String, Function<Object[], Object>> scopeMap =
                IoC.resolve("ioc.scope.create.empty");
            if (args.length > 0) {
              scopeMap.put("ioc.scope.parent", (Object[] innerArgs) -> args[0]);
            } else {
              scopeMap.put(
                  "ioc.scope.parent", (Object[] innerArgs) -> IoC.resolve("ioc.scope.current"));
            }
            return scopeMap;
          });

      /*-
         При регистрации зависимости мы создаем команду RegisterDependencyCommand и передаем ей
         нужные параметры.
         Регистрация новой стратегии разрешения(resolve) зависимости в нашем контейнере.
         Когда мы регистрируем зависимость, то мы создаем команду RegisterDependencyCommand.
         RegisterDependencyCommand - команда, которая будет регистрировать зависимость.
         Param#1 - имя зависимости
         Param#2 - лямбда функция(Function <Object[], Object>), которая будет вызываться
         Usage:
         IoC.<Command>resolve("ioc.register", "A", (Object[] args -> new A()}).execute()
         , где
         - "ioc.register" - имя разрешаемой зависимости
         На вход подается два параметра:
         - "A" - имя зависимости
         - (Object[] args) -> { return new A();} - стратегия(лямбда функция - Function <Object[], Object>)
         с помощью которой будет разрешена эта зависимость.
         Стратегия принимает на вход параметры args(Object[]) и возвращает что-то(Object).
         В примере стратегия возвращает экземпляр класса A.
         Т.е. Когда просят вернуть экземпляр класса A, то будет вызвана стратегия(лямбда функция) и
         результат работы этой лямбда функции вернется на выходе. В примере это Command, которую мы
         выполняем(execute) для регистрации стратегии внутри фабрики.

         В случае Spring-а, "A" - это имя bean-а в Spring контейнере.

         Т.е. Вместо
            SomeInterface user = new User();
            Function<Object[], Object> dependencyResolveStrategy = args -> new User();
            IoC.register("A", dependencyResolveStrategy)

            мы пишем
            IoC.<Command>resolve("ioc.register", "A", dependencyResolveStrategy).execute()

         Второй подход более универсальный, т.к. При появлении новой команды (например IoC.freeze) мы не будем менять существующий клиентский код.
         Usage:
         SomeInterface a = IoC.<SomeInterface>resolve("A");
      */
      scopesMap.putIfAbsent(
          "ioc.register",
          (Object[] args) -> { // регистрация стратегии(strategy) внутри фабрики
            String dependencyName = (String) args[0];
            Function<Object[], Object> strategy = (Function<Object[], Object>) args[1];
            /*-
               Таким образом, метод IoC.resolve() полностью заменяет создание объекта через "new", решая главную задачу:
               клиентский код не меняется при изменении правил создания объектов.
               Ключевое слово "new" будет использоваться только при регистрации зависимости, что
               позволяет соблюдать принцип открытости/замкнутости (Open-Closed Principle).
            */
            return new RegisterDependencyCommand(dependencyName, strategy);
          });

      /*-
         Основная стратегия.
         Стратегия, которая разрешает(resolve) зависимость в виде лямбды функции -
         BiFunction<String, Object[], Object>.
         Когда нас просят разрешить зависимость, то мы в текущем потоке "currentScopeThreadLocal"
         получаем установленный scope - currentScopeThreadLocal.get().
         Если scope есть, то возвращаем его. Если не установлен, то возвращаем rootScope.
         DependencyResolverImpl - специальная конструкция с помощью которой мы находим нужную
         зависимость и вызываем ее(strategyResolver).
      */
      BiFunction<String, Object[], DependencyResolver> strategyResolver =
          new BiFunction<String, Object[], DependencyResolver>() {
            @Override
            public DependencyResolver apply(String dependency, Object[] args) {
              // 1. Находим scope
              Object findScope =
                  currentScopeThreadLocal.get() != null ? currentScopeThreadLocal.get() : scopesMap;
              // 2. Находим нужную зависимость и вызываем ее.
              DependencyResolver dependencyResolver = new DependencyResolverImpl(findScope);
              return (DependencyResolver) dependencyResolver.resolve(dependency, args);
            }
          };

      /*-
         Вызываем зависимость "update.ioc.resolve.dependency.strategy" для того, чтобы заменить на стратегию.
         param1 = имя зависимости "update.ioc.resolve.dependency.strategy" (same as Spring Bean name)
         param2 = стратегия с помощью которой будет разрешена(resolve) эта зависимость.
         Вызываем данную зависимость для того, чтобы заменить ее на стратегию.
      */
      Command command = IoC.resolve("update.ioc.resolve.dependency.strategy", strategyResolver);
      command.execute();

      isAlreadyExecutesSuccessfully.set(true);
    }
  }
}
