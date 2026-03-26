package ru.otus.main_patterns.hw10.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static ru.otus.main_patterns.hw10.util.JwtStatus.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import ru.otus.main_patterns.hw10.util.JwtStatus;
import ru.otus.main_patterns.hw10.util.JwtUtil;

class AuthFilterTest {
  private AuthFilter authFilter;
  private HttpServletRequest requestMock;
  private HttpServletResponse responseMock;
  private FilterChain filterChainMock;
  private ObjectMapper objectMapper;
  private StringWriter responseContent;

  @BeforeEach
  void setUp() throws Exception {
    objectMapper = new ObjectMapper();
    authFilter = new AuthFilter(objectMapper);

    requestMock = mock(HttpServletRequest.class);
    responseMock = mock(HttpServletResponse.class);
    filterChainMock = mock(FilterChain.class);

    // Подготовка для перехвата JSON ответа
    responseContent = new StringWriter();
    PrintWriter writer = new PrintWriter(responseContent);
    when(responseMock.getWriter()).thenReturn(writer);
  }

  @Test
  @DisplayName(
      "Должен вернуть ошибку 401 и сообщение 'Token invalid format' если неверный формат токена")
  void shouldReturnInvalidFormatWhenFormatIsInvalid() throws Exception {
    // Arrange
    String invalidToken = "invalid.token.format";
    when(requestMock.getHeader("Authorization")).thenReturn("Bearer " + invalidToken);
    try (MockedStatic<JwtUtil> mockedJwtUtil = mockStatic(JwtUtil.class)) {
      mockedJwtUtil.when(() -> JwtUtil.validate(invalidToken)).thenReturn(INVALID_FORMAT);

      // Act
      authFilter.doFilter(requestMock, responseMock, filterChainMock);

      // Assert
      verify(responseMock).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      // Проверяем  отсутствие вызова следующего фильтра
      verify(filterChainMock, never()).doFilter(any(), any());

      assertThat(responseContent.toString()).contains(INVALID_FORMAT.getDetail());
    }
  }

  @Test
  @DisplayName(
      "Должен вернуть ошибку 401 и сообщение 'Token invalid signature', если токен подписан неверно")
  void shouldReturnInvalidSignatureWhenSignatureIsInvalid() throws Exception {
    // Arrange
    String badSigToken = "header.payload.badsig";
    when(requestMock.getHeader("Authorization")).thenReturn("Bearer " + badSigToken);
    try (MockedStatic<JwtUtil> mockedJwtUtil = mockStatic(JwtUtil.class)) {
      mockedJwtUtil.when(() -> JwtUtil.validate(badSigToken)).thenReturn(INVALID_SIGNATURE);

      // Act
      authFilter.doFilter(requestMock, responseMock, filterChainMock);

      // Assert
      // Проверяем статус
      verify(responseMock).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      // Проверяем  отсутствие вызова следующего фильтра
      verify(filterChainMock, never()).doFilter(any(), any());

      assertThat(responseContent.toString()).contains(INVALID_SIGNATURE.getDetail());
    }
  }

  @Test
  @DisplayName("Должен вернуть ошибку 401 и сообщение 'Token expired', если токен истек")
  void shouldReturnTokenExpiredWhenSignatureIsInvalid() throws Exception {
    // Arrange
    String tokenExpired = "header.payload.tokenExpired";
    when(requestMock.getHeader("Authorization")).thenReturn("Bearer " + tokenExpired);
    try (MockedStatic<JwtUtil> mockedJwtUtil = mockStatic(JwtUtil.class)) {
      mockedJwtUtil.when(() -> JwtUtil.validate(tokenExpired)).thenReturn(EXPIRED);

      // Act
      authFilter.doFilter(requestMock, responseMock, filterChainMock);

      // Assert
      // Проверяем статус
      verify(responseMock).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      // Проверяем  отсутствие вызова следующего фильтра
      verify(filterChainMock, never()).doFilter(any(), any());

      assertThat(responseContent.toString()).contains(EXPIRED.getDetail());
    }
  }

  @Test
  @DisplayName("Должен пропустить запрос дальше, в случае валидного токена")
  void shouldProceedWhenTokenIsValid() throws Exception {
    // Arrange
    String validToken = "valid.token.here";
    when(requestMock.getHeader("Authorization")).thenReturn("Bearer " + validToken);
    try (MockedStatic<JwtUtil> mockedJwtUtil = mockStatic(JwtUtil.class)) {
      mockedJwtUtil.when(() -> JwtUtil.validate(validToken)).thenReturn(JwtStatus.VALID);

      // Act
      authFilter.doFilter(requestMock, responseMock, filterChainMock);

      // Assert
      // Проверяем, что фильтр пропустил запрос дальше
      verify(filterChainMock).doFilter(requestMock, responseMock);
      verify(responseMock, never()).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }
  }
}
