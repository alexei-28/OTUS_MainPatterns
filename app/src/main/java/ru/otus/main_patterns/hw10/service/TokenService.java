package ru.otus.main_patterns.hw10.service;

import javax.servlet.http.HttpServletRequest;
import ru.otus.main_patterns.hw10.util.JwtUtil;

public class TokenService {

  public void validateToken(HttpServletRequest req) {
    String token = req.getHeader("Authorization");
    if (token != null && token.startsWith("Bearer ")) {
      token = token.substring("Bearer ".length()).trim();
    }
    boolean isValid = token != null && JwtUtil.validate(token);
    if (!isValid) {
      throw new SecurityException("Invalid token");
    }
  }
}
