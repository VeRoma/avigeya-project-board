package com.avigeya.projectboard.service;

import com.avigeya.projectboard.domain.Project;
import com.avigeya.projectboard.domain.ProjectMember;
import com.avigeya.projectboard.domain.Stage;
import com.avigeya.projectboard.domain.User;
import com.avigeya.projectboard.repository.ProjectRepository;
import com.avigeya.projectboard.repository.ProjectMemberRepository;
import com.avigeya.projectboard.repository.ProjectStageRepository;
import com.avigeya.projectboard.repository.StageRepository;
import com.avigeya.projectboard.service.ProjectService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final StageRepository stageRepository;
    private final ProjectStageRepository projectStageRepository;
    private final ProjectMemberRepository projectMemberRepository;

    @Override
    @Transactional
    public void updateProjectStages(Long projectId, List<Long> stageIds) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Проект с ID " + projectId + " не найден."));

        // Удаляем старые связи
        projectStageRepository.deleteByProjectId(projectId);

        // Создаем новые связи
        List<com.avigeya.projectboard.domain.ProjectStage> newProjectStages = stageIds.stream().map(stageId -> {
            Stage stage = new Stage(stageId); // Создаем прокси-объект Stage
            return new com.avigeya.projectboard.domain.ProjectStage(null, project, stage, true);
        }).collect(Collectors.toList());

        projectStageRepository.saveAll(newProjectStages);
        log.info("Этапы для проекта ID {} обновлены. Новые этапы: {}", projectId, stageIds);
    }

    @Override
    @Transactional
    public void updateProjectMembers(Long projectId, List<Long> memberIds, String modifierName) {
        log.info("Пользователь '{}' обновляет участников для проекта ID: {}. Новые участники: {}", modifierName,
                projectId, memberIds);

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Проект с ID " + projectId + " не найден."));

        // Удаляем старые связи
        projectMemberRepository.deleteByProjectId(projectId);

        // Создаем новые связи
        List<ProjectMember> newMembers = memberIds.stream().map(memberId -> {
            User user = new User(memberId); // Создаем прокси-объект User, не загружая его из БД
            return new ProjectMember(null, project, user, true);
        }).collect(Collectors.toList());

        projectMemberRepository.saveAll(newMembers);
    }
}
