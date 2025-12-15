package ru.flish1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Главный класс Spring Boot приложения для интеграции ServiceRoute с 1C:Бухгалтерия
 */
@SpringBootApplication
public class ServiceRouteApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceRouteApplication.class, args);
    }
}

