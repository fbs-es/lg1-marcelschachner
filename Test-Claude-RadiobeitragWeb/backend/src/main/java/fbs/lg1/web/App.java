package fbs.lg1.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Startklasse der Web-Applikation Test-Claude-Radiobeitrag. Startet das Spring-Boot-Backend mit REST-API
 * und Swagger-UI.
 */
@SpringBootApplication
public class App {

    /**
     * Startet die Web-Applikation.
     *
     * @param args die Kommandozeilenargumente
     */
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}
