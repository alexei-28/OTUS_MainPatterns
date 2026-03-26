package ru.otus.main_patterns.hw10.authserver.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class User {
  private String username;

  @JsonProperty("password-hash")
  private String passwordHash;

  // Конструктор по умолчанию нужен Jackson
  public User() {}

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public void setPasswordHash(String passwordHash) {
    this.passwordHash = passwordHash;
  }
}
