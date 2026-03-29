package ru.otus.main_patterns.hw10;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static ru.otus.main_patterns.hw10.command.Operation.MOVE_STRAIGHT;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import freemarker.template.Configuration;
import freemarker.template.Template;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class GameIntegrationTest {
  private static final String AUTH_SERVER_BASE_URL = "http://localhost:8081";
  private static final String GAME_SERVER_BASE_URL = "http://localhost:8080";
  private static final Faker faker = new Faker();
  private static Configuration freemarkerConfig;

  // Запускаем WireMock для имитации AuthServer (8081) и GameServer (8080)
  @RegisterExtension
  static WireMockExtension authService =
      WireMockExtension.newInstance().options(wireMockConfig().port(8081)).build();

  @RegisterExtension
  static WireMockExtension gameService =
      WireMockExtension.newInstance().options(wireMockConfig().port(8080)).build();

  @BeforeAll
  static void setup() {
    freemarkerConfig = new Configuration(Configuration.VERSION_2_3_31);
    freemarkerConfig.setClassForTemplateLoading(GameIntegrationTest.class, "/templates");
  }

  @Test
  void shouldLoginAndCreateOrderSuccessfully() throws Exception {
    // 1. Подготовка данных через Datafaker
    String username = "player-1";
    String passwordHash = "21232ef466bfda2c6bb8526f4cef4f9ecff4095a9f416264de1d33d85eabe04b";
    String mockToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJwbGF5ZXItMSJ9";

    // 2. Настройка WireMock для AuthServer
    Map<String, Object> responseModel = new HashMap<>();
    responseModel.put("token", mockToken);
    String responseJson = processTemplate("auth_success.ftl", responseModel);
    authService.stubFor(
        post(urlEqualTo("/auth/login"))
            .withRequestBody(matchingJsonPath("$.username", equalTo(username)))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(responseJson)));

    // 3. Формируем JSON логина из FTL
    Map<String, Object> loginModel = new HashMap<>();
    loginModel.put("username", username);
    loginModel.put("passwordHash", passwordHash);
    String loginJson = processTemplate("login.ftl", loginModel);

    // 4. Выполняем запрос на авторизацию
    Response loginResponse =
        RestAssured.given()
            .contentType(ContentType.JSON)
            .body(loginJson)
            .post(AUTH_SERVER_BASE_URL + "/auth/login");

    String token = loginResponse.jsonPath().getString("token");
    assertThat(token).isEqualTo(mockToken);

    // 5. Настройка WireMock для GameServer
    gameService.stubFor(
        post(urlEqualTo("/game/order"))
            .withHeader("Authorization", equalTo("Bearer " + token))
            .willReturn(
                aResponse()
                    .withStatus(201)
                    .withHeader("Content-Type", "application/json")
                    .withBody(
                        "{\"orderId\": \""
                            + UUID.randomUUID()
                            + "\", \"message\": \"Operation "
                            + MOVE_STRAIGHT
                            + " successfully created\"}")));

    // 6. Формируем JSON заказа из FTL
    Map<String, Object> orderModel = new HashMap<>();
    orderModel.put("id", UUID.randomUUID().toString());
    orderModel.put("gameId", "game-1");
    orderModel.put("playerId", username);
    orderModel.put("operation", MOVE_STRAIGHT);
    orderModel.put("velocity", faker.number().numberBetween(1, 10));
    String orderJson = processTemplate("order.ftl", orderModel);

    // 7. Выполняем запрос к GameServer на выполнение команды
    Response orderResponse =
        RestAssured.given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body(orderJson)
            .post(GAME_SERVER_BASE_URL + "/game/order");

    // 8. AssertJ проверки
    assertThat(orderResponse.getStatusCode()).isEqualTo(201);
    assertThat(orderResponse.jsonPath().getString("message"))
        .contains("Operation " + MOVE_STRAIGHT + " successfully created");
  }

  // Вспомогательный метод для рендеринга FTL
  private String processTemplate(String templateName, Map<String, Object> model) throws Exception {
    Template template = freemarkerConfig.getTemplate(templateName);
    StringWriter writer = new StringWriter();
    template.process(model, writer);
    return writer.toString();
  }
}
