package ru.otus.main_patterns.hw06.entity;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class VelocityTest {
  @Test
  void simpleEqualsContract() {
    EqualsVerifier.simple().forClass(Velocity.class).verify();
  }
}
