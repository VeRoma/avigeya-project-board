import * as api from './api.js';
import * as render from './ui/render.js';
import * as modals from './ui/modals.js';
import * as uiUtils from './ui/utils.js';
import * as store from './store.js';
import * as auth from './auth.js';

// --- НАЧАЛО ЗАМЕНЫ ФУНКЦИИ ---
/**
 * Сохраняет измененные данные задачи на сервере.
 * @param {object} updatedTaskData - Объект с обновленными данными задачи.
 * @returns {Promise<boolean>} - true в случае успеха, false в случае ошибки.
 */
export async function handleSaveActiveTask(updatedTaskData) {
    // --- ЛОГ: Показываем, какие данные уходят на сервер ---
    console.log('[HANDLERS] > handleSaveActiveTask: Отправляемые данные:', JSON.stringify(updatedTaskData, null, 2));

    try {
        const result = await api.saveTask(updatedTaskData);

        if (result.status === 'success') {
            uiUtils.showMessage('Изменения сохранены', 'success');

            // Обновляем задачу в store, используя данные из ответа сервера
            store.updateTask(result.task);

            // Находим DOM-элемент, чтобы выйти из режима редактирования
            const activeEditElement = document.querySelector(`.task-details.edit-mode[data-version]`);
            const backButtonHandler = uiUtils.getBackButtonHandler();
            if (activeEditElement) {
                uiUtils.exitEditMode(activeEditElement, backButtonHandler);
            }
            uiUtils.updateFabButtonUI(false, null, handleShowAddTaskModal);
            
            // Возвращаем true для индикации успеха
            return true;
        } else {
            throw new Error(result.message || 'Неизвестная ошибка сервера');
        }
    } catch (error) {
        uiUtils.showMessage(`Ошибка сохранения: ${error.message}`, 'error');
        return false;
    }
}
// --- КОНЕЦ ЗАМЕНЫ ФУНКЦИИ ---

export function handleShowAddTaskModal() {
    const appData = store.getAppData();
    modals.openAddTaskModal(store.getAllProjects(), store.getAllUsers(), appData.userRole);
    uiUtils.updateFabButtonUI(true, handleSaveNewTaskClick, handleShowAddTaskModal);
}

export async function handleCreateTask(taskData) {
    const appData = store.getAppData();
    taskData.priority = store.getNextPriorityForStatus(taskData.projectId, taskData.status.id);
    
    const tempTaskId = `temp_${Date.now()}`;
    const optimisticTask = {
        id: tempTaskId,
        name: taskData.name,
        priority: taskData.priority,
        status: taskData.status,
        stage: taskData.stage,
        projectId: taskData.projectId,
        curator: taskData.curator,
        author: taskData.author,
        members: taskData.members,
        version: 0,
    };
    
    store.addTask(optimisticTask);
    modals.closeAddTaskModal();
    
    const accordionState = uiUtils.getAccordionState();
    render.renderProjects(appData.projects, appData.userName, appData.userRole, accordionState, store.getActiveStageFilters());
    uiUtils.showMessage('Задача добавлена, идет сохранение...', 'info');
    
    try {
        const result = await api.addTask(taskData);
        if (result.status === 'success' && result.task) {
            store.replaceTask(tempTaskId, result.task);
            const newAccordionState = uiUtils.getAccordionState();
            render.renderProjects(appData.projects, appData.userName, appData.userRole, newAccordionState, store.getActiveStageFilters());
            uiUtils.showMessage('Задача успешно сохранена', 'success');
        } else {
            throw new Error(result.error || 'Неизвестная ошибка сервера');
        }
    } catch (error) {
        uiUtils.showMessage(`Не удалось сохранить задачу: ${error.message}. Обновляем список...`, 'error');
        setTimeout(() => auth.initializeApp(), 3000);
    }
}

export async function handleStatusUpdate(taskId, newStatusName) {
    const appData = store.getAppData();
    const { task, project } = store.findTask(taskId);
    if (!task || !project) {
        console.error("[HANDLERS.JS ERROR] Task not found in store for ID:", taskId);
        return;
    }
    
    const oldStatusName = task.status.name;
    const oldPriority = task.priority;
    
    const newStatus = store.findStatusByName(newStatusName);
    if (!newStatus) return;

    task.status = newStatus;
    
    if (newStatusName === 'Выполнено') {
        task.priority = 999;
    } else {
        task.priority = store.getNextPriorityForStatus(task.projectId, newStatus.id);
    }

    const tasksInOldGroup = project.tasks.filter(t => t.status.name === oldStatusName && t.id !== task.id);
    tasksInOldGroup.sort((a, b) => a.priority - b.priority).forEach((t, index) => {
        t.priority = index + 1;
    });

    const accordionState = uiUtils.getAccordionState();
    render.renderProjects(appData.projects, appData.userName, appData.userRole, accordionState, store.getStageFilters());
    uiUtils.showMessage('Статус обновлён, идет сохранение...', 'info');
    
    const tasksToUpdate = [...tasksInOldGroup, task].map(t => {
        return {
            taskId: t.id,
            priority: t.priority,
            statusId: t.status.id
        };
    });

    try {
        const result = await api.batchUpdateTaskStatusAndPriorities(tasksToUpdate);
        if (result.status !== 'success') throw new Error(result.error || 'Ошибка сохранения');
        uiUtils.showMessage('Сохранение завершено', 'success');
    } catch (error) {
        console.error('[HANDLERS.JS ERROR] Failed to update priorities:', error);
        uiUtils.showMessage('Не удалось сохранить изменения: ' + error.message, 'error');
        task.status = store.findStatusByName(oldStatusName);
        task.priority = oldPriority;
    }
}

/**
 * Обрабатывает завершение перетаскивания задачи.
 * Отправляет новый порядок задач на сервер для обновления приоритетов.
 * @param {string[]} updatedTaskIds - Массив ID задач в новом порядке.
 */
export async function handleDragDrop(updatedTaskIds) {
    console.log('[HANDLERS] > Новый порядок задач для сохранения:', updatedTaskIds);
    try {
        // Оптимистичное обновление: мы уже передвинули карточку,
        // поэтому просто отправляем запрос на сервер.
        await api.updateTaskPriorities(updatedTaskIds);
        
        // Обновляем приоритеты в локальном хранилище (store)
        updatedTaskIds.forEach((taskId, index) => {
            const { task } = store.findTask(taskId);
            if (task) {
                task.priority = index + 1;
            }
        });

        uiUtils.showMessage('Порядок задач обновлен', 'success');

    } catch (error) {
        console.error('Не удалось сохранить новый порядок задач:', error);
        // Убираем перезагрузку. Просто сообщаем об ошибке.
        // Визуальный порядок останется, но он не будет соответствовать данным на сервере.
        uiUtils.showMessage('Ошибка сохранения порядка. Обновите страницу, чтобы увидеть актуальные данные.', 'error');
    }
}

export function handleSaveNewTaskClick() {
    const appData = store.getAppData();
    
    const taskName = document.getElementById('new-task-name')?.value; // Correct
    const projectId = document.getElementById('new-task-project')?.value; // Correct
    const stageId = document.getElementById('new-task-stage')?.value; // Correct
    const activeStatusElement = document.querySelector('#new-task-status-toggle .toggle-option.active');
    const statusName = activeStatusElement ? activeStatusElement.dataset.status : 'К выполнению';

    const isLimitedView = !['owner', 'admin'].includes(appData.userRole);
    let memberObjects = [];
    if (isLimitedView) {
        const currentUser = store.findUserByName(appData.userName);
        if (currentUser) memberObjects = [currentUser];
    } else {
        const responsibleCheckboxes = document.querySelectorAll('#add-task-modal .user-checkbox:checked');
        const responsibleIds = [...responsibleCheckboxes].map(cb => cb.value);
        memberObjects = store.getUsersByIds(responsibleIds);
    }

    if (!taskName || !projectId || !stageId ) {
        return uiUtils.showMessage('Пожалуйста, заполните все поля: Наименование, Проект, и Этап.', 'error');
    }

    const statusObject = store.findStatusByName(statusName);
    const stageObject = store.getStageById(stageId);
    const authorObject = store.findUserByName(appData.userName);
    const curatorObject = memberObjects.length > 0 ? memberObjects[0] : null;

    handleCreateTask({
        name: taskName,
        projectId: projectId,
        status: statusObject, // Pass full status object
        stage: stageObject, // Pass full stage object
        curator: curatorObject, // Pass full curator object
        author: authorObject, // Pass full author object
        members: memberObjects // Pass array of full member objects
    });
}

export async function handleDeleteTask(taskId) {
    if (!confirm('Вы уверены, что хотите удалить эту задачу?')) {
        return;
    }

    const appData = store.getAppData();
    try {
        uiUtils.showMessage('Удаление задачи...', 'info');
        const result = await api.deleteTask({ taskId: taskId, modifierName: appData.userName });

        if (result.status === 'success') {
            const { project } = store.findTask(taskId);
            if (project) {
                store.removeTask(taskId);
            }
            
            const accordionState = uiUtils.getAccordionState();
            render.renderProjects(appData.projects, appData.userName, appData.userRole, accordionState, store.getStageFilters());
            uiUtils.showMessage('Задача успешно удалена', 'success');
        } else {
            throw new Error(result.error || 'Неизвестная ошибка сервера');
        }
    } catch (error) {
        uiUtils.showMessage(`Не удалось удалить задачу: ${error.message}`, 'error');
    }
}

export async function handleManageMembers(projectId, projectName) {
    try {
        const allUsers = store.getAllUsers();
        const currentMemberIds = (store.getAppData().projectMembers || []) // Используем поле 'active' типа Boolean
            .filter(m => m.projectId == projectId && m.isActive === true)
            .map(m => m.userId);

        modals.openManageMembersModal(projectName, allUsers, currentMemberIds);
        
        const saveBtn = document.getElementById('save-members-btn');
        saveBtn.replaceWith(saveBtn.cloneNode(true));
        document.getElementById('save-members-btn').addEventListener('click', () => {
             handleSaveMembers(projectId);
        });

    } catch (error) {
        uiUtils.showMessage(`Ошибка: ${error.message}`, 'error');
    }
}

async function handleSaveMembers(projectId) {
    const checkedBoxes = document.querySelectorAll('#members-modal-list .member-checkbox:checked');
    const newMemberIds = Array.from(checkedBoxes).map(cb => cb.value);
    
    const appData = store.getAppData();
    
    try {
        uiUtils.showMessage('Сохранение...', 'info');
        const result = await api.updateProjectMembers(projectId, { 
            memberIds: newMemberIds, 
            modifierName: appData.userName 
        });

        if (result.status === 'success') {
            document.getElementById('manage-members-modal').classList.remove('active');
            uiUtils.showMessage('Список участников обновлен!', 'success');
            // Перезагружаем приложение, чтобы обновить все данные, включая связи
            auth.initializeApp();
        } else {
            throw new Error(result.error || 'Неизвестная ошибка сервера');
        }
    } catch (error) {
         uiUtils.showMessage(`Ошибка сохранения: ${error.message}`, 'error');
    }
}

export async function handleManageStages(projectId, projectName) {
    try {
        const appData = store.getAppData();
        const allStages = appData.allStages; // Полный справочник этапов

        // Получаем ID активных этапов из отдельного списка связей projectStages
        const activeStageIds = (appData.projectStages || [])
            .filter(ps => String(ps.projectId) === String(projectId) && ps.isActive)
            .map(ps => ps.stageId);

        console.log(`[HANDLERS] > Открытие модального окна этапов для проекта "${projectName}" (ID: ${projectId})`);
        console.log('[HANDLERS] > Все доступные этапы (справочник):', allStages);
        console.log('[HANDLERS] > ID активных этапов для этого проекта:', activeStageIds);

        modals.openManageStagesModal(projectName, allStages, activeStageIds);
        
        const saveBtn = document.getElementById('save-stages-btn');
        saveBtn.replaceWith(saveBtn.cloneNode(true));
        document.getElementById('save-stages-btn').addEventListener('click', () => {
             handleSaveStages(projectId);
        });

    } catch (error) {
        uiUtils.showMessage(`Ошибка: ${error.message}`, 'error');
    }
}

async function handleSaveStages(projectId) {
    const checkedBoxes = document.querySelectorAll('#stages-modal-list .stage-checkbox:checked');
    const newStageIds = Array.from(checkedBoxes).map(cb => cb.value);

    console.log(`[HANDLERS] > Сохранение этапов для проекта ID: ${projectId}. Выбранные ID:`, newStageIds);
    
    try {
        uiUtils.showMessage('Сохранение...', 'info');
        const result = await api.updateProjectStages(projectId, newStageIds);

        if (result.status === 'success') {
            document.getElementById('manage-stages-modal').classList.remove('active');
            uiUtils.showMessage('Список этапов обновлен!', 'success');
            // Перезагружаем приложение, чтобы обновить все данные, включая связи
            auth.initializeApp();
            const appData = store.getAppData();
            const currentFilters = store.getStageFilters();
            currentFilters[projectId] = newStageIds;
            store.setStageFilters(currentFilters);
            
            const accordionState = uiUtils.getAccordionState();
            render.renderProjects(appData.projects, appData.userName, appData.userRole, accordionState, store.getActiveStageFilters());

        } else {
            throw new Error(result.error || 'Неизвестная ошибка сервера');
        }
    } catch (error) {
         uiUtils.showMessage(`Ошибка сохранения: ${error.message}`, 'error');
    }
}

// public/js/handlers.js

// --- НАЧАЛО ЗАМЕНЫ ФУНКЦИИ ---
export async function handleUpdateTaskMembers(targetElementId, curatorId, memberIds) {
    try {
        // --- КЛЮЧЕВОЕ ИСПРАВЛЕНИЕ ЗДЕСЬ ---
        // Мы получаем 'task-details-1' и превращаем его в '1'
        const taskId = targetElementId.replace('task-details-', '');

        const appData = store.getAppData();
        const payload = {
            curatorId: curatorId,
            memberIds: memberIds,
            modifierName: appData.userName
        };

        // Отправляем на сервер уже правильный, чистый taskId
        const result = await api.updateTaskMembers(taskId, payload);

        if (result.status === 'success') {
            uiUtils.showMessage('Состав исполнителей обновлен!', 'success');
            // Временно перезагружаем все данные для обновления интерфейса
            auth.initializeApp();
        } else {
            throw new Error(result.error || 'Неизвестная ошибка сервера');
        }
    } catch (error) {
        console.error('Ошибка при обновлении исполнителей:', error);
        uiUtils.showMessage(error.message, 'error');
    }
}