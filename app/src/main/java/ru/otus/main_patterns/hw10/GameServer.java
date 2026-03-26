package ru.otus.main_patterns.hw10;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import ru.otus.main_patterns.hw10.filter.AuthFilter;
import ru.otus.main_patterns.hw10.servlet.HelloServlet;
import ru.otus.main_patterns.hw10.servlet.OrderServlet;

public class GameServer {
  private final Server server;

  public GameServer(int port) {
    server = new Server(port);
    ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.SESSIONS);
    handler.addFilter(new FilterHolder(new AuthFilter(new ObjectMapper())), "/game/*", null);
    handler.addServlet(HelloServlet.class, "/hello");
    handler.addServlet(OrderServlet.class, "/game/order");
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
