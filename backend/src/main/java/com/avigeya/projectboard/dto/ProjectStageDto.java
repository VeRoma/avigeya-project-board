package com.avigeya.projectboard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectStageDto {
    private Long id;
    private Long projectId;
    private Long stageId;
    private Boolean isActive;
}