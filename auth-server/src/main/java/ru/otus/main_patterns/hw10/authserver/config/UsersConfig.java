package ru.otus.main_patterns.hw10.authserver.config;

import java.util.Map;
import ru.otus.main_patterns.hw10.authserver.model.User;

public class UsersConfig {
  private Map<String, User> users;

  public UsersConfig() {}

  public Map<String, User> getUsers() {
    return users;
  }

  public void setUsers(Map<String, User> users) {
    this.users = users;
  }
}
