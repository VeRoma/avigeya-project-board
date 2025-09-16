package com.avigeya.projectboard.dto;

import lombok.Data;

@Data
public class InitialDataRequest {
    // Поле для строки initData из Telegram
    private String initData;
    // Поле для ID пользователя в режиме отладки
    private Long debugUserId;
}