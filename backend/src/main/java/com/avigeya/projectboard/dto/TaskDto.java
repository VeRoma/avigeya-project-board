package com.avigeya.projectboard.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class TaskDto {
    private Long id;
    private String name;
    private String message;
    private Integer priority;
    private LocalDate startDate;
    private LocalDate finishDate;
    private StatusDto status;
    private UserDto curator;
    private UserDto author;
    private Long projectId;
    private StageDto stage;
    private List<UserDto> members;
    private Integer version;
}
