package ru.otus.main_patterns.hw05.scopes;

import java.util.Map;
import java.util.function.Function;
import ru.otus.main_patterns.hw05.interfaces.DependencyResolver;

/**
 * Help: <a
 * href="https://github.com/etyumentcev/appserver/blob/main/appserver/scopes/DependencyResolver.cs">DependencyResolver</a>
 */
public class DependencyResolverImpl implements DependencyResolver {
  private final Map<String, Function<Object[], Object>> dependencies;

  public DependencyResolverImpl(Object scope) {
    this.dependencies = (Map<String, Function<Object[], Object>>) scope;
  }

  @Override
  public Object resolve(String dependency, Object[] args) {
    Map<String, Function<Object[], Object>> dependenciesLocal = dependencies;
    while (true) {
      Function<Object[], Object> findDependency = dependenciesLocal.get(dependency);
      if (findDependency != null) {
        // E.g. IoC.resolve<Command>("ioc.register", "A", (object[] args) => new A()).execute()
        return findDependency.apply(args); // e.g. return "new A()"
      } else {
        //  Если в текущем scope нет нужной зависимости, то мы обращаемся к родительскому scope и
        // ищем выше в родительском scope и так далее до самого верхнего scope.
        Function<Object[], Object> findScopeParent = dependenciesLocal.get("ioc.scope.parent");
        dependenciesLocal = (Map<String, Function<Object[], Object>>) findScopeParent.apply(args);
      }
    }
  }
}
