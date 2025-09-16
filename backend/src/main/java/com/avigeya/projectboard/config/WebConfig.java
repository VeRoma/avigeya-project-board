package com.avigeya.projectboard.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/v1/**") // Разрешаем доступ ко всем эндпоинтам нашего API
                .allowedOrigins(
                        "http://localhost:8000", // Адрес, с которого будет запускаться фронтенд
                        "http://127.0.0.1:8000")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Разрешенные HTTP-методы
                .allowedHeaders("*") // Разрешаем все заголовки
                .allowCredentials(true); // Разрешаем передачу credentials (например, cookie)
    }
}