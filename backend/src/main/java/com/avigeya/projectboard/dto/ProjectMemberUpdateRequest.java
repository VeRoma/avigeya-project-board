package com.avigeya.projectboard.dto;

import lombok.Data;
import java.util.List;

@Data
public class ProjectMemberUpdateRequest {
    private List<Long> memberIds;
    private String modifierName;
}