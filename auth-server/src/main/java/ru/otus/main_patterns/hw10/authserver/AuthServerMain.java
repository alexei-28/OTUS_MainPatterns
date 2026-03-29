package ru.otus.main_patterns.hw10.authserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthServerMain {
  private static final Logger logger = LoggerFactory.getLogger(AuthServerMain.class);

  public static void main(String[] args) throws Exception {
    logger.info(
        "Java version: {}, Java vendor: {}",
        System.getProperty("java.version"),
        System.getProperty("java.vendor"));

    AuthServer authServer = new AuthServer(8081);
    authServer.start();
    logger.debug("AuthServer started on http://localhost:8081");
    authServer.join();
  }
}
