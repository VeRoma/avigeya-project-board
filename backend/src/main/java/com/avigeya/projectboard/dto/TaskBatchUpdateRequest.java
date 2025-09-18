package com.avigeya.projectboard.dto;

import lombok.Data;

@Data
public class TaskBatchUpdateRequest {
    private Long taskId;
    private Integer priority;
    private Long statusId;
}
