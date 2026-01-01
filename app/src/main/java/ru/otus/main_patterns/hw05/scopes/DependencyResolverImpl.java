package ru.otus.main_patterns.hw05.scopes;

import java.util.Map;
import java.util.function.Function;
import ru.otus.main_patterns.hw05.interfaces.DependencyResolver;

/**
 * Help: https://github.com/etyumentcev/appserver/blob/main/appserver/scopes/DependencyResolver.cs
 */
public class DependencyResolverImpl implements DependencyResolver {
  private final Map<String, Function<Object[], Object>> dependencyResolverStrategyMap; // scope

  public DependencyResolverImpl(Object scope) {
    this.dependencyResolverStrategyMap = (Map<String, Function<Object[], Object>>) scope;
  }

  @Override
  public Object resolve(String dependency, Object[] args) {
    Map<String, Function<Object[], Object>> currentdDependencyResolverStrategyMap =
        dependencyResolverStrategyMap;
    while (true) {
      Function<Object[], Object> dependencyResolverStrategy =
          currentdDependencyResolverStrategyMap.get(dependency);
      if (dependencyResolverStrategy != null) {
        // E.g. IoC.resolve<Command>("ioc.register", "A", (object[] args) => new A()).execute()
        return dependencyResolverStrategy.apply(args); // e.g. return "new A()"
      } else {
        // В текущем контексте нет нужной зависимости и мы обращаемся к родительскому контексту и
        // ищем выше в родительском контексте.
        // И так далее до самого верхнего контекста.
        Function<Object[], Object> strategy =
            currentdDependencyResolverStrategyMap.get("ioc.scope.parent");
        currentdDependencyResolverStrategyMap =
            (Map<String, Function<Object[], Object>>) strategy.apply(args);
      }
    }
  }
}
