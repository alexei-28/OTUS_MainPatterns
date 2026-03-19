package ru.otus.main_patterns.hw08.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.main_patterns.hw08.dto.Order;
import ru.otus.main_patterns.hw08.service.InterpretCommandService;

public class OrderServlet extends HttpServlet {
  private final ObjectMapper mapper;
  private final InterpretCommandService interpretCommandService;
  private static final Logger logger = LoggerFactory.getLogger(OrderServlet.class);

  public OrderServlet() {
    this.mapper = new ObjectMapper();
    this.interpretCommandService = new InterpretCommandService();
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    Order order = mapper.readValue(req.getReader(), Order.class);
    if (order.getId() == null || order.getId().isEmpty()) {
      order.setId(UUID.randomUUID().toString());
    }
    try {
      interpretCommandService.processMessage(order);

      // Prepare response
      resp.setStatus(HttpServletResponse.SC_CREATED);
      resp.setContentType("application/json");
      resp.setCharacterEncoding("UTF-8");
      Map<String, Object> responseMap = new LinkedHashMap<>();
      responseMap.put("orderId", order.getId());
      responseMap.put("message", "Operation '" + order.getOperationId() + "' successfully created");
      String jsonResponse = mapper.writeValueAsString(responseMap);
      resp.getWriter().write(jsonResponse);
      logger.debug("doPost, successfully created message: {}", jsonResponse);
    } catch (SecurityException ex) {
      resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
      resp.setContentType("application/json");
      resp.setCharacterEncoding("UTF-8");
      Map<String, Object> errorResponse = new LinkedHashMap<>();
      errorResponse.put("orderId", order.getId());
      errorResponse.put("message", ex.getMessage());
      String jsonError = mapper.writeValueAsString(errorResponse);
      resp.getWriter().write(jsonError);
      logger.warn("doPost, Error: {}", jsonError);
    }
  }
}
