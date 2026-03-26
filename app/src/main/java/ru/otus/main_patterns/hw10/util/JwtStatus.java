package ru.otus.main_patterns.hw10.util;

public enum JwtStatus {
  VALID("Token is valid"),
  INVALID_FORMAT("Token invalid format"),
  INVALID_SIGNATURE("Token invalid signature"),
  EXPIRED("Token expired");

  private final String detail;

  JwtStatus(String detail) {
    this.detail = detail;
  }

  public String getDetail() {
    return detail;
  }
}
