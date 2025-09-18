package com.avigeya.projectboard.service;

import com.avigeya.projectboard.domain.Status;
import com.avigeya.projectboard.domain.Task;
import com.avigeya.projectboard.domain.TaskMember;
import com.avigeya.projectboard.domain.User;
import com.avigeya.projectboard.dto.TaskBatchUpdateRequest;
import com.avigeya.projectboard.repository.StatusRepository;
import com.avigeya.projectboard.repository.TaskRepository;
import com.avigeya.projectboard.repository.TaskMemberRepository;
import com.avigeya.projectboard.service.TaskService;
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

        // Обновляем куратора
        if (curatorId != null) {
            task.setCurator(new User(curatorId));
        } else {
            task.setCurator(null);
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
}
