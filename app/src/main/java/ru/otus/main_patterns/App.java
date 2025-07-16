package ru.otus.main_patterns;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {
    private static final Logger logger = LoggerFactory.getLogger(App.class);
    public static void main(String[] args) {
        logger.info("Java version: {}, Java vendor: {}", System.getProperty("java.version")
                , System.getProperty("java.vendor"));

    }
}
