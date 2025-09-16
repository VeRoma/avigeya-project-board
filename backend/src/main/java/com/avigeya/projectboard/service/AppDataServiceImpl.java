package com.avigeya.projectboard.service;

import com.avigeya.projectboard.domain.Project;
import com.avigeya.projectboard.domain.ProjectMember;
import com.avigeya.projectboard.domain.Task;
import com.avigeya.projectboard.domain.TaskMember;
import com.avigeya.projectboard.domain.User;
import com.avigeya.projectboard.dto.AppDataDto;
import com.avigeya.projectboard.dto.ProjectDto;
import com.avigeya.projectboard.dto.StageDto;
import com.avigeya.projectboard.dto.StatusDto;
import com.avigeya.projectboard.dto.TaskDto;
import com.avigeya.projectboard.dto.UserDto;
import com.avigeya.projectboard.exception.ResourceNotFoundException;
import com.avigeya.projectboard.repository.ProjectMemberRepository;
import com.avigeya.projectboard.repository.ProjectRepository;
import com.avigeya.projectboard.repository.StageRepository;
import com.avigeya.projectboard.repository.StatusRepository;
import com.avigeya.projectboard.repository.TaskMemberRepository;
import com.avigeya.projectboard.repository.TaskRepository;
import com.avigeya.projectboard.repository.UserRepository;
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
    private final TaskMemberRepository taskMemberRepository;

    public AppDataServiceImpl(UserRepository userRepository, ProjectRepository projectRepository,
            TaskRepository taskRepository, StatusRepository statusRepository, StageRepository stageRepository,
            ProjectMemberRepository projectMemberRepository, TaskMemberRepository taskMemberRepository) {
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.statusRepository = statusRepository;
        this.stageRepository = stageRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.taskMemberRepository = taskMemberRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public AppDataDto getAppData(Long userId) {
        User currentUser = userRepository.findByTgUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with Telegram ID: " + userId));

        List<Project> userProjects;
        Set<Task> userTasksSet = new HashSet<>();

        String role = currentUser.getRole();
        if ("owner".equals(role) || "admin".equals(role)) {
            userProjects = projectRepository.findAll();
            userTasksSet.addAll(taskRepository.findAll());
        } else {
            userProjects = projectMemberRepository.findByUser(currentUser).stream()
                    .map(ProjectMember::getProject)
                    .toList();
            userTasksSet.addAll(taskRepository.findByCuratorOrAuthor(currentUser, currentUser));
            List<Task> memberTasks = taskMemberRepository.findByUser(currentUser).stream()
                    .map(TaskMember::getTask)
                    .toList();
            userTasksSet.addAll(memberTasks);
        }

        List<Task> actualTasks = userTasksSet.stream()
                .filter(task -> task.getStatus() != null && !"Выполнено".equals(task.getStatus().getName()))
                .toList();

        List<TaskDto> taskDtos = actualTasks.stream()
                .map(this::convertToTaskDto)
                .toList();

        List<ProjectDto> projectDtos = userProjects.stream()
                .map(p -> new ProjectDto(p.getId(), p.getName()))
                .toList();

        List<UserDto> allUserDtos = userRepository.findAll().stream()
                .map(u -> new UserDto(u.getId(), u.getName(), u.getRole()))
                .toList();

        List<StatusDto> allStatusDtos = statusRepository.findAll().stream()
                .map(s -> new StatusDto(s.getId(), s.getName(), s.getIcon(), s.getOrder()))
                .toList();

        List<StageDto> allStageDtos = stageRepository.findAll().stream()
                .map(s -> new StageDto(s.getId(), s.getName()))
                .toList();

        return AppDataDto.builder()
                .currentUserId(currentUser.getId()) // Возвращаем внутренний ID, а не телеграм
                .userName(currentUser.getName())
                .userRole(currentUser.getRole())
                .projects(projectDtos) // Отфильтрованные проекты для пользователя
                .tasks(taskDtos) // Отфильтрованные задачи для пользователя
                .allProjects(
                        projectRepository.findAll().stream().map(p -> new ProjectDto(p.getId(), p.getName())).toList())
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
            dto.setStatus(new StatusDto(task.getStatus().getId(), task.getStatus().getName()));
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
        return dto;
    }
}