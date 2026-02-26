package ru.otus.main_patterns.hw06.entity;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class VectorTest {

  @Test
  void simpleEqualsContract() {
    EqualsVerifier.simple().forClass(Vector.class).verify();
  }
}
