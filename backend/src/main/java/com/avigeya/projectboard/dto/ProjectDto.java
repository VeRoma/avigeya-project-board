package com.avigeya.projectboard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDto {
    private Long id;
    private String name;
    // Список задач, принадлежащих этому проекту
    private List<TaskDto> tasks;

    public ProjectDto(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}
