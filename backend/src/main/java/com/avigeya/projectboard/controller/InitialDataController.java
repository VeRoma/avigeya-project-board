package com.avigeya.projectboard.controller;

import com.avigeya.projectboard.dto.InitialDataDto;
import com.avigeya.projectboard.service.InitialDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class InitialDataController {

    private final InitialDataService initialDataService;

    @Autowired
    public InitialDataController(InitialDataService initialDataService) {
        this.initialDataService = initialDataService;
    }

    @GetMapping("/initial-data")
    public InitialDataDto getInitialData(@RequestParam(defaultValue = "1") Long userId) {
        // Мы передаем userId как параметр запроса, например /api/initial-data?userId=1
        // defaultValue = "1" означает, что если параметр не указан, мы будем
        // по умолчанию считать, что это пользователь с ID=1 (наша "Алёна").
        return initialDataService.getInitialDataForUser(userId);
    }
}