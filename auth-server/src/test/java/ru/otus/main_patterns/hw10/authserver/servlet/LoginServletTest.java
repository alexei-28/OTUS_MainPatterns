package ru.otus.main_patterns.hw10.authserver.servlet;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import freemarker.template.Configuration;
import freemarker.template.Template;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.otus.main_patterns.hw10.authserver.service.UserService;

@ExtendWith(MockitoExtension.class)
class LoginServletTest {
  private LoginServlet loginServlet;
  @Mock private UserService userServiceMock;
  @Mock private HttpServletRequest requestMock;
  @Mock private HttpServletResponse responseMock;
  private StringWriter responseWriter;
  private static final String LOGIN_REQUEST_TEMPLATE = "login_request.ftl";
  private static final String ERROR_RESPONSE_TEMPLATE = "error_response.ftl";
  private static final String JWT_REGEXP = "^[A-Za-z0-9-_=]+\\.[A-Za-z0-9-_=]+\\.[A-Za-z0-9-_=]+$";
  private static final Faker faker = new Faker();
  private static final String USER_NAME = "username";
  private static final String PASSWORD_HASH = "passwordHash";
  private static String expectedErrorResponse;
  private final ObjectMapper mapper = new ObjectMapper();
  private final Map<String, Object> dateMap = new HashMap<>();

  @BeforeEach
  void setUp() throws Exception {
    loginServlet = new LoginServlet(userServiceMock);
    responseWriter = new StringWriter();
    when(responseMock.getWriter()).thenReturn(new PrintWriter(responseWriter));
    expectedErrorResponse =
        processTemplate(
            ERROR_RESPONSE_TEMPLATE,
            new HashMap<String, Object>() {
              {
                put("errorMessage", "Invalid credentials");
              }
            });

    dateMap.clear();
  }

  @Test
  void shouldReturnUnauthorizedWhenMissingPassword() throws Exception {
    // Arrange
    dateMap.put(USER_NAME, faker.name().username());
    String jsonInput = processTemplate(LOGIN_REQUEST_TEMPLATE, dateMap);
    setupMockInput(jsonInput);
    when(userServiceMock.isAuthenticate(anyString(), anyString())).thenReturn(false);

    // Act
    loginServlet.doPost(requestMock, responseMock);

    // Assert
    verify(responseMock).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    assertThat(mapper.readValue(responseWriter.toString(), Map.class))
        .isEqualTo(mapper.readValue(expectedErrorResponse, Map.class));
  }

  @Test
  void shouldReturnUnauthorizedWhenMissingUsername() throws Exception {
    // Arrange
    dateMap.put(PASSWORD_HASH, faker.internet().password());
    String jsonInput = processTemplate(LOGIN_REQUEST_TEMPLATE, dateMap);
    setupMockInput(jsonInput);
    when(userServiceMock.isAuthenticate(anyString(), anyString())).thenReturn(false);

    // Act
    loginServlet.doPost(requestMock, responseMock);

    // Assert
    verify(responseMock).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    assertThat(mapper.readValue(responseWriter.toString(), Map.class))
        .isEqualTo(mapper.readValue(expectedErrorResponse, Map.class));
  }

  @Test
  void shouldReturnUnauthorizedWhenUsernameDoesNotExist() throws Exception {
    // Arrange
    dateMap.put(USER_NAME, faker.name().username());
    dateMap.put(PASSWORD_HASH, faker.internet().password());
    String jsonInput = processTemplate(LOGIN_REQUEST_TEMPLATE, dateMap);
    setupMockInput(jsonInput);
    when(userServiceMock.isAuthenticate(anyString(), anyString())).thenReturn(false);

    // Act
    loginServlet.doPost(requestMock, responseMock);

    // Assert
    verify(responseMock).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    assertThat(mapper.readValue(responseWriter.toString(), Map.class))
        .isEqualTo(mapper.readValue(expectedErrorResponse, Map.class));
  }

  @Test
  void shouldReturnOkAndTokenWhenCredentialsAreCorrect() throws Exception {
    // Arrange
    dateMap.put(USER_NAME, faker.name().username());
    dateMap.put(PASSWORD_HASH, faker.internet().password());
    String jsonInput = processTemplate(LOGIN_REQUEST_TEMPLATE, dateMap);
    setupMockInput(jsonInput);
    when(userServiceMock.isAuthenticate(anyString(), anyString())).thenReturn(true);

    // Act
    loginServlet.doPost(requestMock, responseMock);

    // Assert
    // Проверяем установку правильных заголовков
    verify(responseMock).setContentType("application/json");
    verify(responseMock).setCharacterEncoding("UTF-8");
    String resultJson = responseWriter.toString();
    assertThat(resultJson)
        .isNotBlank()
        .contains("\"token\":")
        .contains("eyJ"); // Стандартное начало JWT токена в Base64 ({"alg"...)

    // Убеждаемся, что статус ошибки (401) НЕ вызывался
    verify(responseMock, never()).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
  }

  @Test
  void shouldReturnDifferentTokensForTwoIdenticalRequests() throws Exception {
    // Arrange
    dateMap.put(USER_NAME, faker.name().username());
    dateMap.put(PASSWORD_HASH, faker.internet().password());
    String jsonInput = processTemplate(LOGIN_REQUEST_TEMPLATE, dateMap);
    when(userServiceMock.isAuthenticate(anyString(), anyString())).thenReturn(true);

    // Act
    // --- ПЕРВЫЙ ЗАПРОС ---
    setupMockInput(jsonInput);
    loginServlet.doPost(requestMock, responseMock);
    String firstResponse = responseWriter.toString();

    // Мы ждем, пока системное время изменится хотя бы на 1 секунду,
    // чтобы JwtUtil сгенерировал новый iat (issued at)
    await()
        .atMost(2, TimeUnit.SECONDS)
        .pollDelay(1, TimeUnit.SECONDS) // Минимальная задержка перед проверкой
        .until(() -> true); // В данном контексте мы просто используем это как умную паузу

    // Очищаем StringWriter для записи второго ответа
    responseWriter.getBuffer().setLength(0);

    // --- ВТОРОЙ ЗАПРОС ---
    setupMockInput(jsonInput);
    loginServlet.doPost(requestMock, responseMock);
    String secondResponse = responseWriter.toString();

    // Assert
    assertThat(firstResponse).contains("\"token\":");
    assertThat(secondResponse).contains("\"token\":");
    assertThat(firstResponse)
        .as("Tokens must be different because they are generated at different seconds")
        .isNotEqualTo(secondResponse);
    verify(responseMock, never()).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
  }

  @Test
  void shouldReturnJwtTokenWhenCredentialsAreValid() throws Exception {
    // Arrange
    dateMap.put(USER_NAME, faker.name().username());
    dateMap.put(PASSWORD_HASH, faker.internet().password());
    String jsonInput = processTemplate(LOGIN_REQUEST_TEMPLATE, dateMap);
    setupMockInput(jsonInput);
    // Имитируем успешную аутентификацию
    when(userServiceMock.isAuthenticate(anyString(), anyString())).thenReturn(true);

    // Act
    loginServlet.doPost(requestMock, responseMock);

    // Assert
    // Проверяем заголовки ответа
    verify(responseMock).setContentType("application/json");
    verify(responseMock).setCharacterEncoding("UTF-8");
    String resultJson = responseWriter.toString();
    assertThat(resultJson).contains("\"token\":").doesNotContain("error");
    // Извлекаем сам токен из строки {"token":"..."}
    String token = resultJson.substring(resultJson.indexOf(":") + 2, resultJson.lastIndexOf("\""));
    // Проверяем формат JWT (три части Base64, разделенные точками)
    // Регулярное выражение для базовой проверки структуры JWT
    assertThat(token).matches(JWT_REGEXP);
  }

  /** Вспомогательный метод для подмены InputStream в запросе */
  private void setupMockInput(String json) throws IOException {
    ByteArrayInputStream inputStream =
        new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
    ServletInputStream servletInputStream =
        new ServletInputStream() {
          @Override
          public int read() throws IOException {
            return inputStream.read();
          }

          @Override
          public boolean isFinished() {
            return inputStream.available() == 0;
          }

          @Override
          public boolean isReady() {
            return true;
          }

          @Override
          public void setReadListener(ReadListener readListener) {}
        };
    when(requestMock.getInputStream()).thenReturn(servletInputStream);
  }

  private String processTemplate(String templateFileName, Map<String, Object> data)
      throws Exception {
    Configuration cfg = new Configuration(Configuration.VERSION_2_3_32);
    cfg.setClassLoaderForTemplateLoading(getClass().getClassLoader(), "templates");
    Template template = cfg.getTemplate(templateFileName);
    StringWriter out = new StringWriter();
    template.process(data, out);
    return out.toString();
  }
}
