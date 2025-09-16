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
    private StatusDto status;
    private UserDto curator;
    private UserDto author;
    private Long projectId;
    private StageDto stage; // <-- Добавлено это поле
}
