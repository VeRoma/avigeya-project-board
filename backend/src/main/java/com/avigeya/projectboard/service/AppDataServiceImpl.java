package com.avigeya.projectboard.service;

import com.avigeya.projectboard.domain.Project;
import com.avigeya.projectboard.domain.ProjectMember;
import com.avigeya.projectboard.domain.Task;
import com.avigeya.projectboard.domain.User;
import com.avigeya.projectboard.dto.*;
import com.avigeya.projectboard.exception.ResourceNotFoundException;
import com.avigeya.projectboard.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class AppDataServiceImpl implements AppDataService {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final StatusRepository statusRepository;
    private final StageRepository stageRepository;
    private final ProjectMemberRepository projectMemberRepository;

    public AppDataServiceImpl(UserRepository userRepository, ProjectRepository projectRepository,
            TaskRepository taskRepository, StatusRepository statusRepository, StageRepository stageRepository,
            ProjectMemberRepository projectMemberRepository) {
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.statusRepository = statusRepository;
        this.stageRepository = stageRepository;
        this.projectMemberRepository = projectMemberRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public AppDataDto getAppData(Long userId) {
        User currentUser = userRepository.findByTgUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with Telegram ID: " + userId));

        String role = currentUser.getRole();

        // --- ШАГ 1: ОПТИМИЗИРОВАННОЕ ПОЛУЧЕНИЕ ЗАДАЧ ---
        // Заменяем множество запросов на один, используя JOIN FETCH.
        Set<Task> userTasksSet;
        if ("owner".equals(role) || "admin".equals(role)) {
            userTasksSet = new HashSet<>(taskRepository.findAllWithDetails());
        } else {
            userTasksSet = taskRepository.findTasksForUserWithDetails(currentUser);
        }

        // --- ШАГ 2: Получаем проекты пользователя (логика осталась прежней) ---
        List<Project> userProjects;
        if ("owner".equals(role) || "admin".equals(role)) {
            userProjects = projectRepository.findAll();
        } else {
            userProjects = projectMemberRepository.findByUser(currentUser).stream()
                    .map(ProjectMember::getProject)
                    .toList();
        }
        List<ProjectDto> projectDtos = userProjects.stream()
                .map(p -> new ProjectDto(p.getId(), p.getName()))
                .toList();

        // --- ШАГ 3: Конвертируем задачи в DTO (теперь это не вызывает доп. запросов) ---
        List<TaskDto> taskDtos = userTasksSet.stream()
                .filter(task -> task.getStatus() != null && !"Выполнено".equals(task.getStatus().getName()))
                .map(this::convertToTaskDto)
                .toList();

        // --- ШАГ 4: Загружаем справочники (это отдельные запросы, и это нормально) ---
        List<UserDto> allUserDtos = userRepository.findAll().stream()
                .map(u -> new UserDto(u.getId(), u.getName(), u.getRole()))
                .toList();
        List<StatusDto> allStatusDtos = statusRepository.findAll().stream()
                .map(s -> new StatusDto(s.getId(), s.getName(), s.getIcon(), s.getOrder()))
                .toList();
        List<StageDto> allStageDtos = stageRepository.findAll().stream()
                .map(s -> new StageDto(s.getId(), s.getName()))
                .toList();
        List<ProjectDto> allProjects = projectRepository.findAll().stream()
                .map(p -> new ProjectDto(p.getId(), p.getName()))
                .toList();

        // --- ШАГ 5: Собираем финальный DTO ---
        return AppDataDto.builder()
                .currentUserId(currentUser.getId())
                .userName(currentUser.getName())
                .userRole(currentUser.getRole())
                .projects(projectDtos)
                .tasks(taskDtos)
                .allProjects(allProjects)
                .allUsers(allUserDtos)
                .allStatuses(allStatusDtos)
                .allStages(allStageDtos)
                .build();
    }

    private TaskDto convertToTaskDto(Task task) {
        TaskDto dto = new TaskDto();
        dto.setId(task.getId());
        dto.setName(task.getName());
        dto.setPriority(task.getPriority());
        dto.setStartDate(task.getStartDate());
        dto.setFinishDate(task.getFinishDate());

        if (task.getStatus() != null) {
            dto.setStatus(new StatusDto(task.getStatus().getId(), task.getStatus().getName(), task.getStatus().getIcon(), task.getStatus().getOrder()));
        }
        if (task.getCurator() != null) {
            dto.setCurator(new UserDto(task.getCurator().getId(), task.getCurator().getName()));
        }
        if (task.getAuthor() != null) {
            dto.setAuthor(new UserDto(task.getAuthor().getId(), task.getAuthor().getName()));
        }
        if (task.getProject() != null) {
            dto.setProjectId(task.getProject().getId());
        }
        if (task.getStage() != null) {
            dto.setStage(new StageDto(task.getStage().getId(), task.getStage().getName()));
        }
        return dto;
    }
}
