package com.avigeya.projectboard.service;

import com.avigeya.projectboard.domain.*;
import com.avigeya.projectboard.dto.TaskBatchUpdateRequest;
import com.avigeya.projectboard.dto.TaskDto;
import com.avigeya.projectboard.dto.StageDto;
import com.avigeya.projectboard.dto.UserDto;
import com.avigeya.projectboard.dto.StatusDto;
import com.avigeya.projectboard.exception.ResourceNotFoundException;
import com.avigeya.projectboard.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final StatusRepository statusRepository;
    private final TaskMemberRepository taskMemberRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final StageRepository stageRepository;

    @Override
    @Transactional
    public TaskDto updateTask(Long taskId, TaskDto taskDto) {
        log.info("Updating task with ID: {}", taskId);
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        // Update simple fields
        task.setName(taskDto.getName());
        task.setMessage(taskDto.getMessage());
        // --- ИСПРАВЛЕНИЕ: Обновляем приоритет, только если он был передан ---
        if (taskDto.getPriority() != null) {
            task.setPriority(taskDto.getPriority());
        }
        task.setStartDate(taskDto.getStartDate());
        task.setFinishDate(taskDto.getFinishDate());
        // НЕ КОПИРУЕМ ВЕРСИЮ! Hibernate управляет этим полем.

        // Update related entities
        if (taskDto.getStatus() != null && taskDto.getStatus().getId() != null) {
            Status status = statusRepository.findById(taskDto.getStatus().getId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Status not found with id: " + taskDto.getStatus().getId()));
            task.setStatus(status);
        }

        if (taskDto.getProjectId() != null) {
            Project project = projectRepository.findById(taskDto.getProjectId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Project not found with id: " + taskDto.getProjectId()));
            task.setProject(project);
        }

        if (taskDto.getStage() != null && taskDto.getStage().getId() != null) {
            Stage stage = stageRepository.findById(taskDto.getStage().getId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Stage not found with id: " + taskDto.getStage().getId()));
            task.setStage(stage);
        }

        // Update members and curator
        if (taskDto.getMembers() != null) {
            // Автор задачи не меняется при обновлении, поэтому мы его не трогаем.
            // Hibernate видит, что поле author не изменилось, и не будет его обновлять.
            String modifierName = taskDto.getAuthor() != null ? taskDto.getAuthor().getName() : "SYSTEM";
            updateTaskMembers(taskId, taskDto.getCurator() != null ? taskDto.getCurator().getId() : null,
                    taskDto.getMembers().stream().map(UserDto::getId).toList(), modifierName);
        }

        Task updatedTask = taskRepository.save(task);
        log.info("Task with ID: {} successfully updated to version {}", updatedTask.getId(), updatedTask.getVersion());
        return convertToDto(updatedTask);
    }

    @Override
    @Transactional
    public void updateTaskPriorities(List<Long> taskIds) {
        if (taskIds == null || taskIds.isEmpty()) {
            log.warn("Получен пустой список ID для обновления приоритетов.");
            return;
        }
        log.info("Обновление приоритетов для {} задач.", taskIds.size());

        List<Task> tasksToUpdate = taskRepository.findAllById(taskIds);

        Map<Long, Task> taskMap = tasksToUpdate.stream()
                .collect(Collectors.toMap(Task::getId, Function.identity()));

        for (int i = 0; i < taskIds.size(); i++) {
            Long taskId = taskIds.get(i);
            Task task = taskMap.get(taskId);

            if (task != null) {
                task.setPriority(i + 1);
            } else {
                log.warn("Задача с ID {} не найдена в базе данных при обновлении приоритета.", taskId);
            }
        }

        taskRepository.saveAll(tasksToUpdate);
        log.info("Приоритеты для {} задач успешно обновлены.", tasksToUpdate.size());
    }

    @Override
    @Transactional
    public void batchUpdateTasks(List<TaskBatchUpdateRequest> updates) {
        if (updates == null || updates.isEmpty()) {
            log.warn("Получен пустой список для пакетного обновления задач.");
            return;
        }
        log.info("Пакетное обновление {} задач.", updates.size());

        for (TaskBatchUpdateRequest update : updates) {
            Task task = taskRepository.findById(update.getTaskId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Задача с ID " + update.getTaskId() + " не найдена при пакетном обновлении."));

            if (update.getPriority() != null) {
                task.setPriority(update.getPriority());
            }

            if (update.getStatusId() != null
                    && (task.getStatus() == null || !task.getStatus().getId().equals(update.getStatusId()))) {
                Status newStatus = statusRepository.findById(update.getStatusId())
                        .orElseThrow(() -> new EntityNotFoundException(
                                "Статус с ID " + update.getStatusId() + " не найден."));
                task.setStatus(newStatus);
            }

            taskRepository.save(task);
        }
        log.info("Пакетное обновление задач завершено.");
    }

    @Override
    @Transactional
    public void updateTaskMembers(Long taskId, Long curatorId, List<Long> memberIds, String modifierName) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Задача с ID " + taskId + " не найдена."));

        // --- ИСПРАВЛЕНИЕ: Обновляем куратора, только если пришел новый ID ---
        if (curatorId != null) {
            task.setCurator(new User(curatorId));
        }

        // Обновляем список участников
        taskMemberRepository.deleteByTaskId(taskId);
        Set<TaskMember> newTaskMembers = memberIds.stream().map(memberId -> {
            TaskMember tm = new TaskMember(null, task, new User(memberId));
            return tm;
        }).collect(Collectors.toSet());
        taskMemberRepository.saveAll(newTaskMembers);
    }

    @Override
    @Transactional
    public void deleteTask(Long taskId) {
        if (!taskRepository.existsById(taskId)) {
            throw new EntityNotFoundException("Задача с ID " + taskId + " не найдена для удаления.");
        }
        // Сначала удаляем все связанные записи в task_members, чтобы избежать ошибок
        taskMemberRepository.deleteByTaskId(taskId);
        taskRepository.deleteById(taskId);
        log.info("Задача с ID {} была успешно удалена.", taskId);
    }

    /**
     * Helper method to convert a Task entity to a TaskDto.
     * 
     * @param task The Task entity.
     * @return The corresponding TaskDto.
     */
    private TaskDto convertToDto(Task task) {
        TaskDto dto = new TaskDto();
        dto.setId(task.getId());
        dto.setName(task.getName());
        dto.setMessage(task.getMessage());
        dto.setPriority(task.getPriority());
        dto.setStartDate(task.getStartDate());
        dto.setFinishDate(task.getFinishDate());
        dto.setVersion(task.getVersion());
        dto.setProjectId(task.getProject().getId());

        if (task.getStatus() != null) {
            dto.setStatus(new StatusDto(task.getStatus().getId(), task.getStatus().getName(),
                    task.getStatus().getIcon(), task.getStatus().getOrder()));
        }
        if (task.getStage() != null) {
            dto.setStage(
                    new StageDto(task.getStage().getId(), task.getStage().getName(), task.getStage().getDescription()));
        }
        if (task.getAuthor() != null) {
            dto.setAuthor(
                    new UserDto(task.getAuthor().getId(), task.getAuthor().getName(), task.getAuthor().getRole()));
        }
        // --- ИСПРАВЛЕНИЕ: Добавляем куратора и участников в ответ ---
        if (task.getCurator() != null) {
            dto.setCurator(
                    new UserDto(task.getCurator().getId(), task.getCurator().getName(), task.getCurator().getRole()));
        }
        if (task.getMembers() != null && !task.getMembers().isEmpty()) {
            List<UserDto> memberDtos = task.getMembers().stream()
                    .map(user -> new UserDto(user.getId(), user.getName(), user.getRole()))
                    .toList();
            dto.setMembers(memberDtos);
        }
        return dto;
    }
}
