// public/js/store.js

// --- НАЧАЛО ЗАМЕНЫ ФАЙЛА ---

// Приватные переменные, доступные только внутри этого модуля
let _appData = {};
let _allProjects = [];
let _allUsers = [];
let _allStatuses = [];
let _taskMap = new Map(); // Кэш для быстрого доступа к задачам по ID
let stageFilters = {};
let activeStageFilters = new Set();


/**
 * Сохраняет все начальные данные приложения в хранилище.
 */
export function setAppData(data) {
    if (!data) return;
    _appData = data;
    _allProjects = data.allProjects || [];
    _allUsers = data.allUsers || [];
    _allStatuses = data.allStatuses || [];
    // Сохраняем новые данные о связях
    _appData.projectMembers = data.projectMembers || [];
    _appData.projectStages = data.projectStages || [];


    // Создаем кэш задач
    _taskMap.clear();
    if (data.projects) {
        for (const project of data.projects) {
            if (project.tasks) {
                for (const task of project.tasks) {
                    _taskMap.set(String(task.id), task);
                }
            }
        }
    }
}

/**
 * Устанавливает все доступные этапы как активные по умолчанию.
 */
export function selectAllStagesByDefault() {
    if (!_appData.allStages) return;
    const allStageIds = _appData.allStages.map(stage => String(stage.id));
    activeStageFilters = new Set(allStageIds);
}

/**
 * Возвращает полный объект данных приложения.
 */
export function getAppData() {
    return _appData;
}

/**
 * Возвращает массив со всеми проектами.
 */
export function getAllProjects() {
    return _allProjects;
}

/**
 * Возвращает массив со всеми сотрудниками.
 */
export function getAllUsers() {
    return _allUsers;
}

/**
 * Возвращает массив со всеми статусами.
 */
export function getAllStatuses() {
    return _allStatuses;
}

/**
 * Находит задачу и ее проект по taskId.
 */
export function findTask(taskId) {
    const task = _taskMap.get(String(taskId));
    if (!task) return { task: null, project: null };

    const project = _appData.projects.find(p => String(p.id) === String(task.projectId));
    return { task, project };
}

/**
 * Находит пользователя по имени.
 */
export function findUserByName(userName) {
    return _allUsers.find(u => u.name === userName);
}

/**
 * Находит пользователя по ID.
 */
export function findUserById(userId) {
    if (!_allUsers) return null;
    // Приводим к строке для надежного сравнения
    return _allUsers.find(u => String(u.id) === String(userId));
}

/**
 * Возвращает массив пользователей по массиву ID.
 */
export function getUsersByIds(userIds) {
    return userIds.map(id => findUserById(id)).filter(Boolean);
}

/**
 * Находит статус по имени.
 */
export function findStatusByName(statusName) {
    if (!_allStatuses) return null;
    return _allStatuses.find(s => s.name === statusName) || null;
}

/**
 * Находит имя проекта по ID.
 * @param {number|string} projectId - ID проекта.
 * @returns {string} Имя проекта или строка-заглушка.
 */
export function getProjectNameById(projectId) {
    if (!projectId) return 'Без проекта';
    if (!_allProjects) return 'Неизвестный проект';
    const project = _allProjects.find(p => String(p.id) === String(projectId));
    return project ? project.name : 'Неизвестный проект';
}

/**
 * Обновляет данные существующей задачи в store.
 */
export function updateTask(updatedTask) {
    const { task: existingTask } = findTask(updatedTask.id);
    if (existingTask) {
        Object.assign(existingTask, updatedTask);
        _taskMap.set(String(updatedTask.id), existingTask);
    }
}

/**
 * Добавляет новую задачу в store.
 */
export function addTask(newTask) {
    const project = _appData.projects.find(p => String(p.id) === String(newTask.projectId));
    if (project) {
        if (!project.tasks) project.tasks = [];
        project.tasks.push(newTask);
        _taskMap.set(String(newTask.id), newTask);
    }
}

/**
 * Заменяет временную задачу на постоянную после сохранения на сервере.
 */
export function replaceTask(tempId, finalTask) {
    const { project } = findTask(tempId);
    if (project) {
        const taskIndex = project.tasks.findIndex(t => String(t.id) === String(tempId));
        if (taskIndex !== -1) {
            project.tasks[taskIndex] = finalTask;
            _taskMap.delete(String(tempId));
            _taskMap.set(String(finalTask.id), finalTask);
        }
    }
}

/**
 * Удаляет задачу из store.
 */
export function removeTask(taskId) {
    const { project } = findTask(taskId);
    if (project) {
        project.tasks = project.tasks.filter(t => String(t.id) !== String(taskId));
        _taskMap.delete(String(taskId));
    }
}

/**
 * Рассчитывает следующий приоритет для задачи в указанном статусе и проекте.
 */
export function getNextPriorityForStatus(projectId, statusId) {
    const project = _appData.projects.find(p => String(p.id) === String(projectId));
    if (!project || !project.tasks) return 1;

    const tasksInStatus = project.tasks.filter(t => t.status && String(t.status.id) === String(statusId));
    const maxPriority = Math.max(0, ...tasksInStatus.map(t => t.priority || 0));
    return maxPriority + 1;
}

export const setStageFilters = (filters) => { stageFilters = filters; };
export const getStageFilters = () => stageFilters;

export const getStageById = (stageId) => {
    if (!stageId) return 'Без этапа';
    if (!_appData || !_appData.allStages) return null;
    return _appData.allStages.find(s => String(s.id) === String(stageId)) || null;
};

// --- КОНЕЦ ЗАМЕНЫ ФАЙЛА ---

export function getActiveStageFilters() {
    return Array.from(activeStageFilters);
}

export function updateStageFilters(stageId, isSelected) {
    if (isSelected) {
        activeStageFilters.add(String(stageId));
    } else {
        activeStageFilters.delete(String(stageId));
    }
}

/**
 * Устанавливает новый набор активных фильтров по этапам.
 * @param {string[]} stageIds - Массив ID этапов, которые должны быть активны.
 */
export function setActiveStageFilters(stageIds) {
    console.log('[STORE] > Установка новых активных фильтров по этапам:', stageIds);
    activeStageFilters = new Set(stageIds.map(String));
}