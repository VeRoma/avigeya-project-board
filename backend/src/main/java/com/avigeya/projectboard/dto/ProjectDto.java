package com.avigeya.projectboard.dto;

import lombok.Data;

import java.util.List;

@Data
public class ProjectDto {
    private Long projectId;
    private String projectName;
    // Список задач, принадлежащих этому проекту
    private List<TaskDto> tasks;
}