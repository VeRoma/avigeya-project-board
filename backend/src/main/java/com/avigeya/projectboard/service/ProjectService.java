package com.avigeya.projectboard.service;

import java.util.List;

public interface ProjectService {

    /**
     * Обновляет список этапов для указанного проекта.
     *
     * @param projectId ID проекта, для которого обновляются этапы.
     * @param stageIds  Список ID новых этапов проекта.
     */
    void updateProjectStages(Long projectId, List<Long> stageIds);

    /**
     * Обновляет список участников для указанного проекта.
     *
     * @param projectId    ID проекта.
     * @param memberIds    Список ID новых участников.
     * @param modifierName Имя пользователя, вносящего изменения (для логирования).
     */
    void updateProjectMembers(Long projectId, List<Long> memberIds, String modifierName);
}
