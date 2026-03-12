package ru.otus.main_patterns.hwo08.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HelloServlet extends HttpServlet {
  private final ObjectMapper mapper = new ObjectMapper();
  private static final Logger logger = LoggerFactory.getLogger(HelloServlet.class);

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    logger.debug("doGet, requestURL = {}", req.getRequestURL());
    resp.setContentType("application/json");

    Map<String, String> response = new HashMap<>();
    response.put("message", "Hello, from server! Current date: " + OffsetDateTime.now());

    resp.getWriter().write(mapper.writeValueAsString(response));
  }
}
