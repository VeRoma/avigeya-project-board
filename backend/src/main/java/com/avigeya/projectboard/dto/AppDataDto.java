package com.avigeya.projectboard.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AppDataDto {
    // Данные текущего пользователя
    private Long currentUserId;
    private String userName;
    private String userRole;

    // Проекты с задачами, отфильтрованными для этого пользователя
    private List<ProjectDto> projects;

    // Справочники, необходимые для работы UI
    private List<ProjectDto> allProjects; // Полный список проектов (id + name)
    private List<UserDto> allUsers;
    private List<StatusDto> allStatuses;
    private List<StageDto> allStages;
}