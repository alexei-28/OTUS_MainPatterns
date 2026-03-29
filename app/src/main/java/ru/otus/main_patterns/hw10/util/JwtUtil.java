package ru.otus.main_patterns.hw10.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Properties;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class JwtUtil {
  private static final ObjectMapper mapper = new ObjectMapper();
  private static final String SECRET;

  static {
    SECRET = loadSecret();
  }

  private static String loadSecret() {
    Properties props = new Properties();
    try (InputStream is = JwtUtil.class.getClassLoader().getResourceAsStream("jwt.properties")) {
      if (is == null) {
        throw new RuntimeException("jwt.properties not found in resources");
      }
      props.load(is);
      String secret = props.getProperty("jwt.secret");
      if (secret == null || secret.isEmpty()) {
        throw new RuntimeException("jwt.secret not configured in jwt.properties");
      }
      return secret;
    } catch (IOException e) {
      throw new RuntimeException("Failed to load jwt.properties", e);
    }
  }

  public static boolean validate(String token) {
    try {
      String[] parts = token.split("\\.");
      if (parts.length != 3) return false;

      String base64Header = parts[0];
      String base64Payload = parts[1];
      String actualSignature = parts[2];
      // Вычисляем подпись заново
      String expectedSignature = hmacSha256(base64Header + "." + base64Payload);
      if (!expectedSignature.equals(actualSignature)) {
        return false;
      }

      // Проверяем срок действия
      byte[] payloadBytes = Base64.getUrlDecoder().decode(base64Payload);
      ObjectNode payloadNode = (ObjectNode) mapper.readTree(payloadBytes);
      long exp = payloadNode.get("exp").asLong();

      return exp > (System.currentTimeMillis() / 1000);
    } catch (Exception e) {
      return false;
    }
  }

  // HMAC-SHA256 + Base64Url
  private static String hmacSha256(String data) throws Exception {
    Mac mac = Mac.getInstance("HmacSHA256");
    mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
    byte[] hmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
    return Base64.getUrlEncoder().withoutPadding().encodeToString(hmac);
  }
}
