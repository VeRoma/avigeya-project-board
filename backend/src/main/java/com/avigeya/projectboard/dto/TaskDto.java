package com.avigeya.projectboard.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class TaskDto {
    private Long id;
    private String name;
    private Integer priority;
    private LocalDate startDate;
    private LocalDate finishDate;
    private StatusDto status; // <-- Используем StatusDto
    private UserDto curator;  // <-- Используем UserDto
    private UserDto author;   // <-- Используем UserDto
    private Long projectId; // <-- Отдаем только ID проекта, а не весь объект
}