package com.avigeya.projectboard.dto;

import com.avigeya.projectboard.domain.Project;
import com.avigeya.projectboard.domain.User;
import lombok.Data;

import java.util.List;

@Data
public class InitialDataDto {
    private User currentUser; // Полная информация о текущем пользователе
    private List<Project> projects; // Список проектов
    private List<TaskDto> tasks; // Список отфильтрованных задач в формате DTO
}