package com.avigeya.projectboard.service;

import com.avigeya.projectboard.dto.TaskBatchUpdateRequest;

import java.util.List;

public interface TaskService {
    void updateTaskPriorities(List<Long> taskIds);

    /**
     * Обновляет статус и/или приоритет для списка задач.
     * 
     * @param updates Список объектов с taskId, priority и statusId для обновления.
     */
    void batchUpdateTasks(List<TaskBatchUpdateRequest> updates);

    /**
     * Обновляет куратора и список участников для задачи.
     *
     * @param taskId       ID задачи.
     * @param curatorId    ID нового куратора.
     * @param memberIds    Список ID новых участников.
     * @param modifierName Имя пользователя, вносящего изменения (для логирования).
     */
    void updateTaskMembers(Long taskId, Long curatorId, List<Long> memberIds, String modifierName);

    /**
     * Удаляет задачу по ее ID.
     *
     * @param taskId ID задачи для удаления.
     */
    void deleteTask(Long taskId);
}
