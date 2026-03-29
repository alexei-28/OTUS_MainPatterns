package ru.otus.main_patterns.hw10.authserver;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import ru.otus.main_patterns.hw10.authserver.service.UserService;
import ru.otus.main_patterns.hw10.authserver.servlet.HelloServlet;
import ru.otus.main_patterns.hw10.authserver.servlet.LoginServlet;

/*
   Микросервис, который выдает jwt токен из участников космического сражения,
   для того, чтобы игровой сервер мог принять решение о возможности выполнения входящего сообщения от имени пользователя.
*/
public class AuthServer {
  private final Server server;

  public AuthServer(int port) {
    server = new Server(port);
    ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.SESSIONS);
    handler.addServlet(HelloServlet.class, "/hello");
    handler.addServlet(new ServletHolder(new LoginServlet(new UserService())), "/auth/login");
    server.setHandler(handler);
  }

  public void start() throws Exception {
    server.start();
  }

  public void join() throws InterruptedException {
    server.join();
  }

  public void stop() throws Exception {
    server.stop();
  }
}
