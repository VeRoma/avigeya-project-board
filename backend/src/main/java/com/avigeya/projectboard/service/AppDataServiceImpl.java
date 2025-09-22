package com.avigeya.projectboard.service;

import com.avigeya.projectboard.domain.Project;
import com.avigeya.projectboard.domain.ProjectMember;
import com.avigeya.projectboard.domain.Task;
import com.avigeya.projectboard.domain.User;
import com.avigeya.projectboard.dto.*;
import com.avigeya.projectboard.exception.ResourceNotFoundException;
import com.avigeya.projectboard.repository.*;
import com.avigeya.projectboard.service.AppDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AppDataServiceImpl implements AppDataService {

        private final UserRepository userRepository;
        private final ProjectRepository projectRepository;
        private final TaskRepository taskRepository;
        private final StatusRepository statusRepository;
        private final StageRepository stageRepository;
        private final ProjectMemberRepository projectMemberRepository;
        private final ProjectStageRepository projectStageRepository;

        @Override
        @Transactional(readOnly = true)
        public AppDataDto getAppData(Long userId) {
                User currentUser = userRepository.findByTgUserId(userId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "User not found with Telegram ID: " + userId));

                String role = currentUser.getRole();

                // 1. Оптимизированное получение задач
                Set<Task> userTasksSet;
                if ("owner".equals(role) || "admin".equals(role)) {
                        userTasksSet = new HashSet<>(taskRepository.findAllWithDetails());
                } else {
                        userTasksSet = taskRepository.findTasksForUserWithDetails(currentUser);
                }

                // 2. Получение проектов пользователя
                List<Project> userProjects;
                if ("owner".equals(role) || "admin".equals(role)) {
                        userProjects = projectRepository.findAll();
                } else {
                        userProjects = projectMemberRepository.findByUser(currentUser).stream()
                                        .map(ProjectMember::getProject) // Заменено на ссылку на метод
                                        .distinct()
                                        .toList();
                }
                List<ProjectDto> projectDtos = userProjects.stream()
                                .map(p -> new ProjectDto(p.getId(), p.getName()))
                                .toList();

                // 3. Конвертация задач в DTO
                List<TaskDto> taskDtos = userTasksSet.stream()
                                .filter(task -> task.getStatus() != null
                                                && !"Выполнено".equals(task.getStatus().getName()))
                                .map(this::convertToTaskDto)
                                .toList();

                // 4. Загрузка справочников
                List<UserDto> allUserDtos = userRepository.findAll().stream()
                                .map(u -> new UserDto(u.getId(), u.getName(), u.getRole()))
                                .toList();
                List<StatusDto> allStatusDtos = statusRepository.findAll().stream()
                                .map(s -> new StatusDto(s.getId(), s.getName(), s.getIcon(), s.getOrder()))
                                .toList();
                List<StageDto> allStageDtos = stageRepository.findAll().stream()
                                .map(s -> new StageDto(s.getId(), s.getName(), s.getDescription()))
                                .toList();
                List<ProjectDto> allProjects = projectRepository.findAll().stream()
                                .map(p -> new ProjectDto(p.getId(), p.getName()))
                                .toList();
                List<ProjectMemberDto> projectMembers = projectMemberRepository.findAll().stream()
                                .map(pm -> new ProjectMemberDto(pm.getId(), pm.getProject().getId(),
                                                pm.getUser().getId(),
                                                pm.getIsActive()))
                                .toList();
                List<ProjectStageDto> projectStages = projectStageRepository.findAll().stream()
                                .map(ps -> new ProjectStageDto(ps.getId(), ps.getProject().getId(),
                                                ps.getStage().getId(),
                                                ps.getIsActive()))
                                .toList();

                // 5. Сборка финального DTO
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
                                .projectMembers(projectMembers)
                                .projectStages(projectStages)
                                .build();
        }

        private TaskDto convertToTaskDto(Task task) {
                TaskDto dto = new TaskDto();
                dto.setId(task.getId());
                dto.setName(task.getName());
                dto.setPriority(task.getPriority());
                dto.setStartDate(task.getStartDate());
                dto.setMessage(task.getMessage());
                dto.setFinishDate(task.getFinishDate());

                if (task.getStatus() != null) {
                        dto.setStatus(new StatusDto(task.getStatus().getId(), task.getStatus().getName(),
                                        task.getStatus().getIcon(), task.getStatus().getOrder()));
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
                        dto.setStage(
                                        new StageDto(task.getStage().getId(), task.getStage().getName(),
                                                        task.getStage().getDescription()));
                }
                if (task.getMembers() != null && !task.getMembers().isEmpty()) {
                        List<UserDto> memberDtos = task.getMembers().stream()
                                        .map(user -> new UserDto(user.getId(), user.getName(), user.getRole())) // Предполагаем,
                                                                                                                // что
                                                                                                                // UserDto
                                                                                                                // конструктор
                                                                                                                // принимает
                                                                                                                // роль
                                        .toList();
                        dto.setMembers(memberDtos);
                }
                // Гарантируем, что версия никогда не будет null
                dto.setVersion(task.getVersion());
                return dto;
        }
}
