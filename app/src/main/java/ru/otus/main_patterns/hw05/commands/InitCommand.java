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

/*
    Scope = context - это разные области видимости.
    ThreadLocal — это специальный класс в Java, который позволяет создавать переменные, доступные только для одного конкретного потока.
    Если обычная статическая переменная видна всем потокам одинаково, то в случае с ThreadLocal у каждого потока будет своя собственная,
    изолированная копия этой переменной. Изменение значения в одном потоке никак не повлияет на значение в другом.

    ThreadLocal - это карта, где в качестве ключа выступает идентификатор текущего потока(Thread). Можно по текущему потоку хранить какой-то тип данных.
    В результате в каждый поток можно положить собственный scope(context).
    В итоге, когда мы будем обращаться к этой стратегии(через метод resolve), то мы сначала идем в ThreadLocal,
    смотрим в текущем потоке какой текущий контекст(scope) и уже работаем с ним.
    В итоге получается, что в каждом потоке могут быть как одинаковые, так и разные контексты.
    Т.е. Получаем, что благодаря ThreadLocal, в одном потоке один контекст, а в другом потоке другой контекст.
    Т.е. Когда мы выполняем метод resolve, первое, что происходит - это мы пойдем в ThreadLocal и получим текущий контекст.
    В каком бы потоке мы не вызывали мы всегда получим ссылку на тот контекст, который актуален для этого потока.
    Таким образом мы получаем потокобезопасные реализации.
*/
/**
 * Инициализация многопоточного контейнера. Выполняется только один раз. Эта команда инициализирует
 * IoC контейнер для работы с несколькими потоками.
 */
public class InitCommand implements Command {
  private static final ThreadLocal<Object> currentScopeThreadLocal =
      ThreadLocal.withInitial(() -> null);
  // key = dependency name, value = strategy as lambda Function<Object[], Object>
  private static final ConcurrentHashMap<String, Function<Object[], Object>> rootScopesMap =
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
    synchronized (rootScopesMap) {
      // SetCurrentScopeCommand - команда установит, в этом потоке, текущий контекст(смена
      // контекста), который будет передан в качестве аргумента(args[0]).
      // E.g. change scope:
      // IoC.resolve<Command>("ioc.scope.current.set", otherScope).execute()
      rootScopesMap.putIfAbsent(
          "ioc.scope.current.set", (Object[] args) -> new SetCurrentScopeCommand(args[0]));
      rootScopesMap.putIfAbsent(
          "ioc.scope.current.clear", (Object[] args) -> new ClearCurrentScopeCommand());

      // ioc.scope.current - возвращает текущий scope, который есть в текущем потоке.
      // currentScopeThreadLocal.get() - возвращает значение из текущего потока.
      rootScopesMap.putIfAbsent(
          "ioc.scope.current",
          (Object[] args) ->
              currentScopeThreadLocal.get() != null
                  ? currentScopeThreadLocal.get()
                  : rootScopesMap);
      // У контекста можно прочитать родительский контекст. Все контексты упорядочены иерархически.
      // "ioc.scope.parent" - возвращает ссылку на родительский контекст.
      rootScopesMap.putIfAbsent(
          "ioc.scope.parent",
          (Object[] args) -> {
            throw new RuntimeException("The root scope has no a parent scope.");
          });
      rootScopesMap.putIfAbsent(
          "ioc.scope.create.empty",
          (Object[] args) -> new HashMap<String, Function<Object[], Object>>());
      rootScopesMap.putIfAbsent(
          "ioc.scope.create",
          (Object[] args) -> {
            Map<String, Function<Object[], Object>> scopeMap =
                IoC.resolve("ioc.scope.create.empty");
            if (args.length > 0) {
              Object parentScope = args[0];
              scopeMap.put("ioc.scope.parent", (Object[] innerArgs) -> parentScope);
            } else {
              Object parentScope = IoC.resolve("ioc.scope.current");
              scopeMap.put("ioc.scope.parent", (Object[] innerArgs) -> parentScope);
            }
            return scopeMap;
          });

      // Когда мы регистрируем зависимость, то мы создаем команду RegisterDependencyCommand.
      // RegisterDependencyCommand - команда, которая будет регистрировать зависимость.
      // Param#1 - args[0] - имя зависимости
      // Param#2 - args[1] - лямбда функция, которая будет вызываться
      rootScopesMap.putIfAbsent(
          "ioc.register",
          (Object[] args) -> { // регистрация стратегии(strategy) внутри фабрики
            String dependencyName = (String) args[0];
            Function<Object[], Object> strategy = (Function<Object[], Object>) args[1];
            return new RegisterDependencyCommand(dependencyName, strategy);
          });
      logger.debug("execute, rootScopeMap({}): {}", rootScopesMap.size(), rootScopesMap);

      // Основная стратегия
      // Стратегия, которая разрешает(resolve) зависимость в виде лямбда функции -
      // BiFunction<String, Object[], Object>.
      // Когда нас просят разрешить зависимость, то мы в текущем потоке "currentScopeThreadLocal"
      // получаем установленный scope - currentScopeThreadLocal.get()
      // Если он есть, то возвращаем его. Если не установлен, то возвращаем rootScope.
      // DependencyResolverImpl - специальная конструкция с помощью которой мы находим нужную
      // зависимость и вызываем ее(strategyResolver).
      BiFunction<String, Object[], DependencyResolver> strategyResolver =
          (dependencyName, strategy) -> {
            Object findScope =
                currentScopeThreadLocal.get() != null
                    ? currentScopeThreadLocal.get()
                    : rootScopesMap;
            return new DependencyResolverImpl(findScope);
          };
      // Вызываем зависимость "update.ioc.resolve.dependency.strategy" для того, чтобы заменить на
      // стратегию.
      // param1 = имя зависимости "update.ioc.resolve.dependency.strategy" (same as Spring Bean
      // name)
      // param2 = стратегия с помощью которой будет разрешена(resolve) эта зависимость
      IoC.<Command>resolve("update.ioc.resolve.dependency.strategy", strategyResolver).execute();

      isAlreadyExecutesSuccessfully.set(true);
    }
  }
}
