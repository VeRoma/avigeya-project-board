package com.avigeya.projectboard.controller;

import com.avigeya.projectboard.dto.TaskBatchUpdateRequest;
import com.avigeya.projectboard.dto.TaskDto;
import com.avigeya.projectboard.dto.ApiResponse;
import com.avigeya.projectboard.dto.TaskMemberUpdateRequest;
import com.avigeya.projectboard.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PutMapping("/{taskId}")
    public ResponseEntity<ApiResponse> updateTask(@PathVariable Long taskId, @RequestBody TaskDto taskDto) {
        TaskDto updatedTask = taskService.updateTask(taskId, taskDto);
        return ResponseEntity.ok(new ApiResponse("success", "Task updated successfully", updatedTask));
    }

    /**
     * Обновляет приоритеты для списка задач.
     * Принимает отсортированный список ID задач и устанавливает им приоритет
     * в соответствии с их порядком в списке.
     *
     * @param taskIds Отсортированный список ID задач.
     * @return ResponseEntity с сообщением об успехе.
     */
    @PutMapping("/priorities")
    public ResponseEntity<Void> updateTaskPriorities(@RequestBody List<Long> taskIds) {
        taskService.updateTaskPriorities(taskIds);
        return ResponseEntity.ok().build();
    }

    /**
     * Пакетно обновляет статус и/или приоритет для списка задач.
     *
     * @param updates Список объектов с taskId, priority и statusId для обновления.
     * @return ResponseEntity с сообщением об успехе.
     */
    @PutMapping("/batch-update")
    public ResponseEntity<Void> batchUpdateTasks(@RequestBody List<TaskBatchUpdateRequest> updates) {
        taskService.batchUpdateTasks(updates);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{taskId}/members")
    public ResponseEntity<Void> updateTaskMembers(@PathVariable Long taskId,
            @RequestBody TaskMemberUpdateRequest request) {
        taskService.updateTaskMembers(taskId, request.getCuratorId(), request.getMemberIds(),
                request.getModifierName());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long taskId) {
        taskService.deleteTask(taskId);
        return ResponseEntity.noContent().build(); // 204 No Content - стандартный ответ для успешного DELETE
    }
}
