package com.avigeya.projectboard.service;

import com.avigeya.projectboard.dto.AppDataDto;

public interface AppDataService {

    AppDataDto getAppData(Long userId);

}