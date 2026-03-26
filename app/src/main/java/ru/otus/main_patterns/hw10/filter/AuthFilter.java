package ru.otus.main_patterns.hw10.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.main_patterns.hw10.util.JwtStatus;
import ru.otus.main_patterns.hw10.util.JwtUtil;

public class AuthFilter implements Filter {
  private final ObjectMapper mapper;
  private static final Logger logger = LoggerFactory.getLogger(AuthFilter.class);

  public AuthFilter(ObjectMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    HttpServletRequest req = (HttpServletRequest) request;
    HttpServletResponse resp = (HttpServletResponse) response;
    String header = req.getHeader("Authorization");
    String token = null;
    if (header != null && header.startsWith("Bearer ")) {
      token = header.substring("Bearer ".length()).trim();
    }
    JwtStatus jwtStatus = JwtUtil.validate(token);
    if (token == null || !JwtStatus.VALID.equals(jwtStatus)) {
      resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      resp.setContentType("application/json");
      resp.setCharacterEncoding("UTF-8");
      Map<String, Object> errorResponse = new LinkedHashMap<>();
      errorResponse.put("message", jwtStatus.getDetail());
      String jsonError = mapper.writeValueAsString(errorResponse);
      resp.getWriter().write(jsonError);
      logger.warn("doFilter, Error: {}", jsonError);
      return;
    }

    // Всё ок → идём дальше в servlet
    chain.doFilter(request, response);
  }
}
