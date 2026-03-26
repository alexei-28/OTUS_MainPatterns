package ru.otus.main_patterns.hw10.authserver.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Map;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.main_patterns.hw10.authserver.service.UserService;
import ru.otus.main_patterns.hw10.authserver.util.JwtUtil;

public class LoginServlet extends HttpServlet {
  private final ObjectMapper jsonMapper = new ObjectMapper();
  private final UserService userService;
  private static final Logger logger = LoggerFactory.getLogger(LoginServlet.class);

  // Dependency injection
  public LoginServlet(UserService userService) {
    this.userService = userService;
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    try {
      Map<String, String> body = jsonMapper.readValue(req.getInputStream(), Map.class);
      String username = body.get("username");
      String passwordHash = body.get("passwordHash");
      if (userService.isAuthenticate(username, passwordHash)) {
        String token = JwtUtil.createToken(username);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(String.format("{\"token\":\"%s\"}", token));
      } else {
        resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        resp.getWriter().write("{\"error\":\"Invalid credentials\"}");
      }
    } catch (Exception e) {
      logger.error("Auth error", e);
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Auth error: " + e.getMessage());
    }
  }
}
