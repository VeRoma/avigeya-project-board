package com.avigeya.projectboard.service;

import com.avigeya.projectboard.domain.Project;
import com.avigeya.projectboard.domain.ProjectMember;
import com.avigeya.projectboard.domain.Task;
import com.avigeya.projectboard.domain.TaskMember;
import com.avigeya.projectboard.domain.User;
import com.avigeya.projectboard.dto.InitialDataDto;
import com.avigeya.projectboard.dto.StatusDto;
import com.avigeya.projectboard.dto.TaskDto;
import com.avigeya.projectboard.dto.UserDto;
import com.avigeya.projectboard.repository.ProjectMemberRepository;
import com.avigeya.projectboard.repository.ProjectRepository;
import com.avigeya.projectboard.repository.TaskMemberRepository;
import com.avigeya.projectboard.repository.TaskRepository;
import com.avigeya.projectboard.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class InitialDataService {

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final TaskMemberRepository taskMemberRepository;

    @Autowired
    public InitialDataService(ProjectRepository projectRepository, TaskRepository taskRepository,
                              UserRepository userRepository, ProjectMemberRepository projectMemberRepository,
                              TaskMemberRepository taskMemberRepository) {
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.taskMemberRepository = taskMemberRepository;
    }

    /**
     * Собирает все начальные данные для пользователя в зависимости от его роли.
     * @param userId ID пользователя.
     * @return DTO со всеми необходимыми данными.
     */
    @Transactional(readOnly = true)
    public InitialDataDto getInitialDataForUser(Long userId) {
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        List<Project> userProjects;
        // Используем Set, чтобы избежать дублирования задач
        Set<Task> userTasksSet = new HashSet<>();

        String role = currentUser.getRole();
        if ("owner".equals(role) || "admin".equals(role)) {
            // Для админа/владельца загружаем все проекты и все задачи
            userProjects = projectRepository.findAll();
            userTasksSet.addAll(taskRepository.findAll());
        } else {
            // Для остальных пользователей применяем фильтрацию
            // 1. Находим проекты, в которых пользователь является участником
            userProjects = projectMemberRepository.findByUser(currentUser).stream()
                    .map(ProjectMember::getProject)
                    .collect(Collectors.toList());

            // 2. Находим задачи, где пользователь является автором или куратором
            userTasksSet.addAll(taskRepository.findByCuratorOrAuthor(currentUser, currentUser));

            // 3. Находим задачи, в которых пользователь является участником
            List<Task> memberTasks = taskMemberRepository.findByUser(currentUser).stream()
                    .map(TaskMember::getTask)
                    .collect(Collectors.toList());
            userTasksSet.addAll(memberTasks);
        }

        // Фильтруем итоговый список задач, убирая "Выполненные"
        List<Task> actualTasks = userTasksSet.stream()
                .filter(task -> task.getStatus() != null && !task.getStatus().getName().equals("Выполнено"))
                .collect(Collectors.toList());

        // Конвертируем отфильтрованные задачи в DTO
        List<TaskDto> taskDtos = actualTasks.stream()
                .map(this::convertToTaskDto)
                .collect(Collectors.toList());

        // Собираем финальный объект для ответа
        InitialDataDto initialData = new InitialDataDto();
        initialData.setCurrentUser(currentUser);
        initialData.setProjects(userProjects);
        initialData.setTasks(taskDtos);

        return initialData;
    }

    /**
     * Вспомогательный метод для конвертации сущности Task в TaskDto.
     * @param task Сущность Task из базы данных.
     * @return Упрощенный объект TaskDto.
     */
    private TaskDto convertToTaskDto(Task task) {
        TaskDto dto = new TaskDto();
        dto.setId(task.getId());
        dto.setName(task.getName());
        dto.setPriority(task.getPriority());
        dto.setStartDate(task.getStartDate());
        dto.setFinishDate(task.getFinishDate());

        if (task.getStatus() != null) {
            StatusDto statusDto = new StatusDto();
            statusDto.setId(task.getStatus().getId());
            statusDto.setName(task.getStatus().getName());
            dto.setStatus(statusDto);
        }

        if (task.getCurator() != null) {
            UserDto curatorDto = new UserDto();
            curatorDto.setId(task.getCurator().getId());
            curatorDto.setName(task.getCurator().getName());
            dto.setCurator(curatorDto);
        }

        if (task.getAuthor() != null) {
            UserDto authorDto = new UserDto();
            authorDto.setId(task.getAuthor().getId());
            authorDto.setName(task.getAuthor().getName());
            dto.setAuthor(authorDto);
        }

        if (task.getProject() != null) {
            dto.setProjectId(task.getProject().getId());
        }

        return dto;
    }
}