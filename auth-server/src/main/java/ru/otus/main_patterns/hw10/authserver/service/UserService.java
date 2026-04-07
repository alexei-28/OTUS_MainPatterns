package ru.otus.main_patterns.hw10.authserver.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.main_patterns.hw10.authserver.config.UsersConfig;
import ru.otus.main_patterns.hw10.authserver.model.User;

public class UserService {
  private static final Logger logger = LoggerFactory.getLogger(UserService.class);
  private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
  private UsersConfig usersConfig;

  public UserService() {
    loadConfig();
  }

  private void loadConfig() {
    // Используем контекстный загрузчик потока для надежности в контейнерах сервлетов
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    try (InputStream is = classLoader.getResourceAsStream("users-config.yml")) {
      if (is == null) {
        logger.error("Resource users-config.yml not found!");
        throw new RuntimeException("users-config.yml not found in resources");
      }
      this.usersConfig = yamlMapper.readValue(is, UsersConfig.class);
      logger.info(
          "Successfully loaded {} users from users-config.yml",
          usersConfig.getUsers() != null ? usersConfig.getUsers().size() : 0);
    } catch (IOException e) {
      logger.error("Error parsing users-config.yml", e);
      throw new RuntimeException("Failed to load users configuration", e);
    }
  }

  public boolean isAuthenticate(String inputUser, String inputPassHash) {
    if (usersConfig == null
        || usersConfig.getUsers() == null
        || (inputUser == null || inputUser.isEmpty())
        || (inputPassHash == null || inputPassHash.isEmpty())) {
      return false;
    }
    // Оптимизируем: сначала ищем по ключу (player-1),
    // так как это быстрее, чем полный перебор stream().anyMatch()
    // 1. Попытка найти напрямую по ключу (если ID игрока и есть логин)
    Map<String, User> usersMap = usersConfig.getUsers();
    if (usersMap.containsKey(inputUser)) {
      User user = usersMap.get(inputUser);
      if (inputPassHash.equals(user.getPasswordHash())) {
        return true;
      }
    }
    // 2. Если не нашли по ключу, ищем по полю username внутри объектов
    return usersMap.values().stream()
        .anyMatch(
            u -> inputUser.equals(u.getUsername()) && inputPassHash.equals(u.getPasswordHash()));
  }
}
