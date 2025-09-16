package com.avigeya.projectboard.controller;

import com.avigeya.projectboard.dto.AppDataDto;
import com.avigeya.projectboard.service.AppDataService;
import com.avigeya.projectboard.service.TelegramValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(InitialDataController.class);

    private final AppDataService appDataService;
    private final TelegramValidationService telegramValidationService;

    public InitialDataController(AppDataService appDataService, TelegramValidationService telegramValidationService) {
        this.appDataService = appDataService;
        this.telegramValidationService = telegramValidationService;
        log.info("InitialDataController has been successfully initialized.");
    }

    @PostMapping("/app-data")
    public ResponseEntity<AppDataDto> getInitialData(@RequestBody InitialDataRequest request) {
        log.info("Received request for /app-data. Debug user: {}, Init data present: {}",
                request.getDebugUserId(), request.getInitData() != null && !request.getInitData().isEmpty());

        Long userId = null;

        if (request.getDebugUserId() != null) {
            userId = request.getDebugUserId();
            log.info("Using debug mode. UserId set to: {}", userId);
        } else if (request.getInitData() != null) {
            String initData = request.getInitData();
            log.info("Validating initData from Telegram...");
            if (!telegramValidationService.isDataSafe(initData)) {
                log.warn("Validation failed for initData. Returning 403 Forbidden.");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            Map<String, String> userData = telegramValidationService.parseInitData(initData);
            String userIdStr = userData.get("id");
            if (userIdStr != null) {
                userId = Long.parseLong(userIdStr);
                log.info("Successfully parsed userId from initData: {}", userId);
            }
        }

        if (userId == null) {
            log.error("Could not determine userId from the request. Returning 400 Bad Request.");
            return ResponseEntity.badRequest().build();
        }

        log.info("Fetching app data for userId: {}", userId);
        AppDataDto appData = appDataService.getAppData(userId);
        log.info("Successfully fetched app data. Returning 200 OK.");
        return ResponseEntity.ok(appData);
    }
}
