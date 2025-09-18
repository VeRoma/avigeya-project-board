package com.avigeya.projectboard.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**") // Применяем ко всем эндпоинтам, начинающимся с /api/
                .allowedOrigins("*")       // Разрешаем запросы с любого источника
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Разрешаем все основные методы
                .allowedHeaders("*");      // Разрешаем все заголовки
    }
}
