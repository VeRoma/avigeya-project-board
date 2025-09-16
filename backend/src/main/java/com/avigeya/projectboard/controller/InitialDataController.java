package com.avigeya.projectboard.controller;

import com.avigeya.projectboard.dto.AppDataDto;
import com.avigeya.projectboard.dto.InitialDataRequest;
import com.avigeya.projectboard.service.AppDataService;
import com.avigeya.projectboard.service.TelegramValidationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class InitialDataController {

    private final AppDataService appDataService;
    private final TelegramValidationService telegramValidationService;

    public InitialDataController(AppDataService appDataService, TelegramValidationService telegramValidationService) {
        this.appDataService = appDataService;
        this.telegramValidationService = telegramValidationService;
    }

    @PostMapping("/app-data")
    public ResponseEntity<AppDataDto> getInitialData(@RequestBody InitialDataRequest request) {
        Long userId = null;

        // Определяем ID пользователя: либо из отладочного параметра, либо из данных
        // Telegram
        if (request.getDebugUserId() != null) {
            // Режим отладки
            userId = request.getDebugUserId();
        } else if (request.getInitData() != null) {
            // Рабочий режим: валидируем данные от Telegram
            if (!telegramValidationService.isDataSafe(request.getInitData())) {
                // Если данные не прошли проверку, возвращаем ошибку 403 Forbidden
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            // Парсим ID пользователя из initData
            Map<String, String> userData = telegramValidationService.parseInitData(request.getInitData());
            String userIdStr = userData.get("id");
            if (userIdStr != null) {
                userId = Long.parseLong(userIdStr);
            }
        }

        // Если ID пользователя так и не был определен, это плохой запрос
        if (userId == null) {
            return ResponseEntity.badRequest().build();
        }

        // Собираем все данные для фронтенда через сервис
        AppDataDto appData = appDataService.getAppData(userId);
        return ResponseEntity.ok(appData);
    }
}