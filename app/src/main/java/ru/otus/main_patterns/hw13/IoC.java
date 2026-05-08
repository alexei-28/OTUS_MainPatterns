package ru.otus.main_patterns.hw13;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * IoC-контейнер (Inversion of Control) — реализация паттернов Dependency Injection / Service
 * Locator.
 *
 * <p>Предоставляет двухуровневую систему разрешения зависимостей:
 *
 * <ul>
 *   <li><b>Глобальный уровень</b> — зависимости, доступные всем участникам.
 *   <li><b>Уровень scope</b> — изолированные зависимости для конкретного игрока/контекста.
 * </ul>
 *
 * <pre>
 *   Global IoC (общие зависимости)
 *      ↑ fallback
 *   Player Scope (player1)  — приватные зависимости игрока 1
 *   Player Scope (player2)  — приватные зависимости игрока 2
 * </pre>
 *
 * <p>При разрешении зависимости из scope сначала ищется локальная фабрика, а при её отсутствии —
 * происходит fallback на глобальный контейнер.
 */
public class IoC {

  /** Глобальный реестр фабрик: ключ зависимости → фабричная функция. */
  private static final Map<String, Function<Object[], Object>> container = new HashMap<>();

  /** Изолированные scope: идентификатор scope → локальный реестр фабрик. */
  private static final Map<String, Map<String, Function<Object[], Object>>> scopes =
      new HashMap<>();

  /**
   * Регистрирует фабрику зависимости в глобальном контейнере.
   *
   * @param key уникальный ключ зависимости
   * @param factory фабричная функция, принимающая аргументы и возвращающая экземпляр
   */
  public static void register(String key, Function<Object[], Object> factory) {
    container.put(key, factory);
  }

  /**
   * Регистрирует фабрику зависимости в указанном scope. Если scope ещё не существует — он будет
   * создан автоматически.
   *
   * @param scopeId идентификатор scope (например, ID игрока)
   * @param key уникальный ключ зависимости внутри scope
   * @param factory фабричная функция
   */
  public static void register(String scopeId, String key, Function<Object[], Object> factory) {
    createScope(scopeId);
    scopes.get(scopeId).put(key, factory);
  }

  /**
   * Создаёт новый scope, если он ещё не существует.
   *
   * @param scopeId идентификатор scope
   */
  public static void createScope(String scopeId) {
    scopes.putIfAbsent(scopeId, new HashMap<>());
  }

  /**
   * Разрешает зависимость из глобального контейнера.
   *
   * @param key ключ зависимости
   * @param args аргументы, передаваемые в фабричную функцию
   * @param <T> ожидаемый тип возвращаемого объекта
   * @return экземпляр, созданный фабрикой
   * @throws RuntimeException если зависимость с указанным ключом не найдена
   */
  @SuppressWarnings("unchecked")
  public static <T> T resolve(String key, Object... args) {
    Function<Object[], Object> factory = container.get(key);
    if (factory == null) {
      throw new RuntimeException("Dependency not found: " + key);
    }
    return (T) factory.apply(args);
  }

  /**
   * Разрешает зависимость из указанного scope с fallback на глобальный контейнер.
   *
   * <p>Порядок поиска:
   *
   * <ol>
   *   <li>Локальный scope (по {@code scopeId})
   *   <li>Глобальный контейнер (если в scope не найдено)
   * </ol>
   *
   * @param scopeId идентификатор scope
   * @param key ключ зависимости
   * @param args аргументы для фабричной функции
   * @param <T> ожидаемый тип возвращаемого объекта
   * @return экземпляр, созданный фабрикой
   * @throws RuntimeException если зависимость не найдена ни в scope, ни глобально
   */
  @SuppressWarnings("unchecked")
  public static <T> T resolve(String scopeId, String key, Object... args) {
    Map<String, Function<Object[], Object>> scope = scopes.get(scopeId);
    if (scope != null && scope.containsKey(key)) {
      return (T) scope.get(key).apply(args);
    }
    return resolve(key, args);
  }

  /**
   * Полностью очищает глобальный контейнер и все scope. Используется для сброса состояния
   * (например, в тестах).
   */
  public static void clear() {
    container.clear();
    scopes.clear();
  }
}
