package ru.otus.main_patterns.hw10.authserver.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Properties;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/*
    JWT (JSON Web Token) — это компактный и самодостаточный способ безопасно передавать информацию между сторонами
    (обычно клиент ↔ сервер) в виде JSON-объекта.
    1. Общая структура JWT
      JWT всегда состоит из трёх частей, разделённых точками:
        header.payload.signature
    2. Header (заголовок)
        {
          "alg": "HS256",
          "typ": "JWT"
        }

    - alg — алгоритм подписи
        (например: HS256, RS256)
    - typ — тип токена (JWT)
    Header кодируется в Base64Url

    3. Payload (полезная нагрузка)
       Это данные, которые передаем.
    {
      "sub": "1234567890",
      "name": "John Doe",
      "admin": true
    }

    Registered (стандартные)
        iss — кто выдал токен (issuer)
        sub — субъект (обычно userId)
        aud — для кого токен
        exp — время истечения
        nbf — не раньше какого времени
        iat — когда выдан
        jti — уникальный ID токена

    Payload НЕ шифруется, только кодируется!

    4. Signature (подпись)
    Подпись считается так:
    signature = HMACSHA256(
        base64UrlEncode(header) + "." + base64UrlEncode(payload),
        secret

    5. Алгоритмы подписи
        Симметричные:
            - HS256, HS384, HS512
        один секретный ключ
        Асимметричные:
            - RS256 (RSA)
            - ES256 (ECDSA)
        приватный ключ подписывает
        публичный — проверяет

    6. JWT — stateless
       Сервер НЕ хранит сессию

    Example:
    JWT (Срок действия 1 час = 1774645855):
      header.payload.signature

    {
      "header" : {
        "alg" : "HS256",
        "typ" : "JWT"
      },
      "payload" : {
        "sub" : "player-1",
        "exp" : 1774645855
      },
      "signature" : "5bEcEVRD8BEcm2vCICNVkIQi0G5-horg9LJ5WDb2ARs"
    }
    Raw:
    eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJwbGF5ZXItMSIsImV4cCI6MTc3NDY0NTg1NX0.5bEcEVRD8BEcm2vCICNVkIQi0G5-horg9LJ5WDb2ARs
*/
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

  public static String createToken(String username) throws Exception {
    // 1. Header
    ObjectNode headerNode = mapper.createObjectNode();
    headerNode.put("alg", "HS256");
    headerNode.put("typ", "JWT");
    String headerJson = mapper.writeValueAsString(headerNode);

    // 2. Payload (срок жизни 15 минут)
    long expSec = (System.currentTimeMillis() / 1000) + 900;
    ObjectNode payloadNode = mapper.createObjectNode();
    payloadNode.put("sub", username);
    payloadNode.put("exp", expSec);
    String payloadJson = mapper.writeValueAsString(payloadNode);

    // 3. Base64Url кодирование без padding
    String base64Header =
        Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(headerJson.getBytes(StandardCharsets.UTF_8));
    String base64Payload =
        Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));

    // 4. Подпись
    String signature = hmacSha256(base64Header + "." + base64Payload);

    // 5. Собираем JWT
    return base64Header + "." + base64Payload + "." + signature;
  }

  // HMAC-SHA256 + Base64Url
  private static String hmacSha256(String data) throws Exception {
    Mac mac = Mac.getInstance("HmacSHA256");
    mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
    byte[] hmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
    return Base64.getUrlEncoder().withoutPadding().encodeToString(hmac);
  }
}
