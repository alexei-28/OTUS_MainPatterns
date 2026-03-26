package ru.otus.main_patterns.hw10;

import static org.assertj.core.api.Assertions.assertThat;

import freemarker.template.Configuration;
import freemarker.template.Template;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.*;
import ru.otus.main_patterns.hw10.authserver.AuthServer;
import ru.otus.main_patterns.hw10.util.JwtStatus;

class GameIntegrationTest {
  private static final int AUTH_SERVER_PORT = 8081;
  private static final String AUTH_SERVER_BASE_URL = "http://localhost:" + AUTH_SERVER_PORT;

  private static final int GAME_SERVER_PORT = 8080;
  private static final String GAME_SERVER_BASE_URL = "http://localhost:" + GAME_SERVER_PORT;
  private static GameServer gameServer;
  private static AuthServer authServer;
  private static Configuration freemarkerConfig;
  private static final String TOKEN = "token";
  private static final String LOGIN_REQUEST_TEMPLATE = "login_request.ftl";
  private static final String ORDER_REQUEST_TEMPLATE = "order_request.ftl";
  private static final String ID = "id";
  private static final String USER_NAME = "username";
  private static final String PASSWORD_HASH = "passwordHash";
  private static final String VALID_USER_NAME = "player-1";
  private static final String VALID_USER_PASSWORD = "player-1-pass";
  private static final String JWT_EXPIRED =
      "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJwbGF5ZXItMSIsImV4cCI6MTc3NDk5OTUzM30.jzbQd3s6VUdU1Y8W77fM-nCfQySxLt7hnlEOBdxGSv8";

  @BeforeAll
  static void setup() throws Exception {
    // Настройка FreeMarker
    freemarkerConfig = new Configuration(Configuration.VERSION_2_3_31);
    freemarkerConfig.setClassForTemplateLoading(GameIntegrationTest.class, "/templates");

    // Запуск реального AuthServer
    authServer = new AuthServer(AUTH_SERVER_PORT);
    authServer.start();

    // ЗАПУСК РЕАЛЬНОГО ИГРОВОГО СЕРВЕРА
    gameServer = new GameServer(GAME_SERVER_PORT);
    gameServer.start();
  }

  @AfterAll
  static void tearDown() throws Exception {
    if (gameServer != null) {
      gameServer.stop();
    }

    if (authServer != null) {
      authServer.stop();
    }
  }

  @Test
  @DisplayName(
      "Должен вернуть ошибку 401 и сообщение 'Token invalid format' если неверный формат токена")
  void shouldReturnInvalidFormatWhenFormatIsInvalid() throws Exception {
    // Arrange
    Map<String, Object> dataCredentialsMap = new HashMap<>();
    dataCredentialsMap.put(USER_NAME, VALID_USER_NAME);
    dataCredentialsMap.put(PASSWORD_HASH, sha256(VALID_USER_PASSWORD));

    // Act
    // Реальный HTTP-запрос, который уходит во внешний сервис(AuthServer) - получение JWT токена
    Response authResponse =
        RestAssured.given()
            .contentType(ContentType.JSON)
            .body(processTemplate(LOGIN_REQUEST_TEMPLATE, dataCredentialsMap))
            .post(AUTH_SERVER_BASE_URL + "/auth/login");
    String receivedToken = authResponse.jsonPath().getString(TOKEN);

    // Assert
    assertThat(receivedToken).isNotNull();

    // Arrange
    Map<String, Object> dataOrderMap = new HashMap<>();
    dataOrderMap.put(ID, UUID.randomUUID().toString());

    // Act
    // Реальный HTTP-запрос, который уходит во внешний сервис(GameServer) с jwt токеном в header-е
    Response gameServerResponse =
        RestAssured.given()
            .header("Authorization", "Bearer " + receivedToken.replaceAll("\\.", "separator"))
            .contentType(ContentType.JSON)
            .body(processTemplate(ORDER_REQUEST_TEMPLATE, dataOrderMap))
            .post(GAME_SERVER_BASE_URL + "/game/order");

    // Assert
    assertThat(gameServerResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED_401);
    assertThat(gameServerResponse.jsonPath().getString("message"))
        .isEqualTo(JwtStatus.INVALID_FORMAT.getDetail());
  }

  @Test
  @DisplayName(
      "Должен вернуть ошибку 401 и сообщение 'Token invalid signature', если токен подписан неверно")
  void shouldReturnInvalidSignatureWhenSignatureIsInvalid() throws Exception {
    // Arrange
    Map<String, Object> dataCredentialsMap = new HashMap<>();
    dataCredentialsMap.put(USER_NAME, VALID_USER_NAME);
    dataCredentialsMap.put(PASSWORD_HASH, sha256(VALID_USER_PASSWORD));

    // Act
    // Реальный HTTP-запрос, который уходит во внешний сервис(AuthServer) - получение JWT токена
    Response authResponse =
        RestAssured.given()
            .contentType(ContentType.JSON)
            .body(processTemplate(LOGIN_REQUEST_TEMPLATE, dataCredentialsMap))
            .post(AUTH_SERVER_BASE_URL + "/auth/login");
    String receivedToken = authResponse.jsonPath().getString(TOKEN);

    // Assert
    assertThat(receivedToken).isNotNull();

    // Arrange
    Map<String, Object> dataOrderMap = new HashMap<>();
    dataOrderMap.put(ID, UUID.randomUUID().toString());

    // Act
    // Реальный HTTP-запрос, который уходит во внешний сервис(GameServer) с jwt токеном в header-е
    Response gameServerResponse =
        RestAssured.given()
            .header("Authorization", "Bearer " + receivedToken + "_invalid")
            .contentType(ContentType.JSON)
            .body(processTemplate(ORDER_REQUEST_TEMPLATE, dataOrderMap))
            .post(GAME_SERVER_BASE_URL + "/game/order");

    // Assert
    assertThat(gameServerResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED_401);
    assertThat(gameServerResponse.jsonPath().getString("message"))
        .isEqualTo(JwtStatus.INVALID_SIGNATURE.getDetail());
  }

  @Test
  @DisplayName("Должен вернуть ошибку 401 и сообщение 'Token expired', если токен истек")
  void shouldReturnTokenExpiredWhenSignatureIsInvalid() throws Exception {
    // Arrange
    Map<String, Object> dataOrderMap = new HashMap<>();
    dataOrderMap.put(ID, UUID.randomUUID().toString());

    // Act
    // Реальный HTTP-запрос, который уходит во внешний сервис(GameServer) с "протухшим" jwt токеном
    // в header-е
    Response gameServerResponse =
        RestAssured.given()
            .header("Authorization", "Bearer " + JWT_EXPIRED)
            .contentType(ContentType.JSON)
            .body(processTemplate(ORDER_REQUEST_TEMPLATE, dataOrderMap))
            .post(GAME_SERVER_BASE_URL + "/game/order");

    // Assert
    assertThat(gameServerResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED_401);
    assertThat(gameServerResponse.jsonPath().getString("message"))
        .isEqualTo(JwtStatus.EXPIRED.getDetail());
  }

  @Test
  @DisplayName("Должен выполнить команду 'MOVE_STRAIGHT', когда JWT токен валиден")
  void shouldExecuteCommandWhenJwtIsValid() throws Exception {
    // Arrange
    Map<String, Object> dataCredentialsMap = new HashMap<>();
    dataCredentialsMap.put(USER_NAME, VALID_USER_NAME);
    dataCredentialsMap.put(PASSWORD_HASH, sha256(VALID_USER_PASSWORD));

    // Act
    // Реальный HTTP-запрос, который уходит во внешний сервис(AuthServer) - получение JWT токена
    Response authResponse =
        RestAssured.given()
            .contentType(ContentType.JSON)
            .body(processTemplate(LOGIN_REQUEST_TEMPLATE, dataCredentialsMap))
            .post(AUTH_SERVER_BASE_URL + "/auth/login");
    String receivedToken = authResponse.jsonPath().getString(TOKEN);

    // Assert
    assertThat(receivedToken).isNotNull();

    // Arrange
    Map<String, Object> dataOrderMap = new HashMap<>();
    dataOrderMap.put(ID, UUID.randomUUID().toString());

    // Act
    // Реальный HTTP-запрос, который уходит во внешний сервис(GameServer) с jwt токеном в header-е
    Response gameServerResponse =
        RestAssured.given()
            .header("Authorization", "Bearer " + receivedToken)
            .contentType(ContentType.JSON)
            .body(processTemplate(ORDER_REQUEST_TEMPLATE, dataOrderMap))
            .post(GAME_SERVER_BASE_URL + "/game/order");

    // Assert
    assertThat(gameServerResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED_201);
    assertThat(gameServerResponse.jsonPath().getString("message"))
        .isEqualTo("Operation 'MOVE_STRAIGHT' successfully created");
  }

  private String processTemplate(String templateName, Map<String, Object> model) throws Exception {
    Template template = freemarkerConfig.getTemplate(templateName);
    StringWriter writer = new StringWriter();
    template.process(model, writer);
    return writer.toString();
  }

  /** Метод для хэширования строки с SHA-256 */
  private String sha256(String input) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
      // Преобразуем байты в hex вручную
      StringBuilder hexString = new StringBuilder();
      for (byte b : hash) {
        String hex = Integer.toHexString(0xff & b);
        if (hex.length() == 1) hexString.append('0'); // ведущий ноль
        hexString.append(hex);
      }
      return hexString.toString();
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("SHA-256 algorithm not available", e);
    }
  }
}
