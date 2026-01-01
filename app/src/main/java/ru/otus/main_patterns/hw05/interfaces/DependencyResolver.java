package ru.otus.main_patterns.hw05.interfaces;

/** Находит нужную зависимость. */
public interface DependencyResolver {

  Object resolve(String dependencyName, Object[] args);
}
