package com.avigeya.projectboard.dto;

import lombok.Data;
import java.util.List;

@Data
public class TaskMemberUpdateRequest {
    private Long curatorId;
    private List<Long> memberIds;
    private String modifierName;
}