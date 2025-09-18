package com.avigeya.projectboard.controller;

import com.avigeya.projectboard.dto.ProjectMemberUpdateRequest;
import com.avigeya.projectboard.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    /**
     * Обновляет список активных этапов для конкретного проекта.
     *
     * @param projectId ID проекта.
     * @param stageIds  Список ID этапов, которые должны быть активны.
     * @return ResponseEntity с сообщением об успехе.
     */
    @PutMapping("/{projectId}/stages")
    public ResponseEntity<Void> updateProjectStages(@PathVariable Long projectId, @RequestBody List<Long> stageIds) {
        projectService.updateProjectStages(projectId, stageIds);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{projectId}/members")
    public ResponseEntity<Void> updateProjectMembers(@PathVariable Long projectId,
            @RequestBody ProjectMemberUpdateRequest request) {
        projectService.updateProjectMembers(projectId, request.getMemberIds(), request.getModifierName());
        return ResponseEntity.ok().build();
    }
}