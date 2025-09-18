// Базовый URL вашего Java-бэкенда.
// Если вы запускаете фронтенд и бэкенд на одной машине, 'http://localhost:8080' - то, что нужно.
// При развертывании на сервере здесь будет реальный адрес вашего бэкенда.
const API_BASE_URL = 'http://localhost:8080/api/v1';

/**
 * Отправляет унифицированный запрос на бэкенд и обрабатывает базовые ошибки.
 * @param {string} endpoint - Путь к эндпоинту (например, '/app-data').
 * @param {object} options - Параметры для fetch() (method, headers, body).
 * @returns {Promise<any>} - Распарсенный JSON-ответ.
 */
async function fetchApi(endpoint, options = {}) {
    // --- ДОБАВЛЕНО: Логирование исходящего запроса ---
    console.log(`[API] > Отправка запроса: ${options.method || 'GET'} ${API_BASE_URL}${endpoint}`);
    if (options.body) {
        console.log(`[API] > Тело запроса:`, options.body);
    }
    // ---------------------------------------------
    try {
        // --- ДОБАВЛЕНО: Логирование полного объекта запроса ---
        const fetchOptions = {
            headers: {
                'Content-Type': 'application/json',
                ...options.headers,
            },
            ...options,
        };
        console.log(`[API] > Полные параметры для fetch:`, JSON.parse(JSON.stringify(fetchOptions)));
        // ----------------------------------------------------

        const response = await fetch(`${API_BASE_URL}${endpoint}`, fetchOptions);

        console.log(`[API] < Получен ответ. Статус: ${response.status} ${response.statusText}`);

        if (!response.ok) {
            // --- УЛУЧШЕННОЕ ЛОГИРОВАНИЕ ОШИБОК ---
            // Попытаемся прочитать тело ответа как текст, так как ошибка может быть не в формате JSON
            console.error(`[API] < Ошибка! Статус: ${response.status}. Пытаюсь прочитать тело ответа...`);
            const errorText = await response.text();
            console.error(`--- НАЧАЛО ТЕЛА ОШИБКИ (Status: ${response.status}) ---`);
            console.error(errorText);
            console.error(`--- КОНЕЦ ТЕЛА ОШИБКИ ---`);

            // Попробуем распарсить как JSON для стандартной обработки
            try {
                const errorData = JSON.parse(errorText);
                throw new Error(errorData.message || `HTTP ошибка! Статус: ${response.status}`);
            } catch (e) {
                // Если это не JSON, выбрасываем общую ошибку
                throw new Error(`HTTP ошибка! Статус: ${response.status}. Тело ответа не в формате JSON (см. консоль).`);
            }
        }

        // Если у ответа нет тела (например, статус 204 No Content), возвращаем success
        if (response.status === 204) {
            return { status: 'success' };
        }

        // --- ИСПРАВЛЕНИЕ: Обработка пустых ответов ---
        // Сначала получаем ответ как текст
        const responseText = await response.text();

        // Если текст пустой (как в нашем случае с PUT /priorities), 
        // возвращаем успешный объект, не пытаясь парсить JSON.
        if (!responseText) {
            return { status: 'success' };
        }

        // Если текст есть, парсим его как JSON
        return JSON.parse(responseText);
    } catch (error) {
        console.error(`API call to ${endpoint} failed:`, error);
        // Перебрасываем ошибку дальше, чтобы ее можно было обработать в UI
        throw error;
    }
}

/**
 * Загружает начальные данные для приложения.
 * @param {object} payload - Объект, содержащий либо `initData`, либо `debugUserId`.
 */
export const getAppData = (payload) => {
    // Эндпоинт на бэкенде, который принимает initData и возвращает все данные
    return fetchApi(`/app-data`, {
        method: 'POST',
        body: JSON.stringify(payload),
    });
};

/**
 * Сохраняет изменения в существующей задаче.
 * @param {object} payload - Объект с данными задачи.
 */
export function saveTask(payload) {
    // Используем PUT для идемпотентного обновления существующего ресурса
    return fetchApi(`/tasks/${payload.id}`, {
        method: 'PUT',
        body: JSON.stringify(payload),
    });
}

/**
 * Добавляет новую задачу.
 * @param {object} payload - Объект с данными новой задачи.
 */
export const addTask = (payload) => {
    // REST-подход: используем POST для создания нового ресурса
    return fetchApi('/tasks', {
        method: 'POST',
        body: JSON.stringify(payload),
    });
};

/**
 * Удаляет задачу.
 * @param {object} payload - Объект, содержащий taskId.
 */
export const deleteTask = (payload) => {
    // REST-подход: используем DELETE для удаления ресурса
    return fetchApi(`/tasks/${payload.taskId}`, {
        method: 'DELETE',
    });
};

/**
 * Отправляет на сервер новый порядок задач для обновления их приоритетов.
 * @param {string[]} taskIds - Массив ID задач в новом порядке.
 * @returns {Promise<object>}
 */
export async function updateTaskPriorities(taskIds) {
    // Используем новый эндпоинт, который мы создали на бэкенде
    return fetchApi(`/tasks/priorities`, {
        method: 'PUT',
        body: JSON.stringify(taskIds),
    });
}

/**
 * Отправляет на сервер список задач для пакетного обновления их статусов и/или приоритетов.
 * @param {Array<object>} updates - Массив объектов, каждый из которых содержит taskId, priority и statusId.
 * @returns {Promise<object>}
 */
export async function batchUpdateTaskStatusAndPriorities(updates) {
    return fetchApi(`/tasks/batch-update`, {
        method: 'PUT',
        body: JSON.stringify(updates),
    });
}

// Старая функция updatePriorities больше не используется и может быть удалена.
// Она была предназначена для другого эндпоинта и формата данных.
// Если вы хотите сохранить ее для справки, оставьте закомментированной.
/*
export const updatePriorities = (payload) => {
    return fetchApi('/tasks/reorder', { method: 'PUT', body: JSON.stringify(payload.tasks) });
};
*/

/**
 * Загружает все связи (участники проектов, этапы проектов и т.д.).
 */
export const fetchAllConnections = () => {
    return fetchApi('/connections/all');
};

/**
 * Обновляет участников проекта.
 * @param {string} projectId - ID проекта.
 * @param {object} payload - Объект с массивом ID участников.
 */
export const updateProjectMembers = (projectId, payload) => {
    return fetchApi(`/projects/${projectId}/members`, {
        method: 'PUT',
        body: JSON.stringify(payload),
    });
};

/**
 * Обновляет этапы проекта.
 * @param {string} projectId - ID проекта.
 * @param {object} payload - Объект с массивом ID этапов.
 */
export const updateProjectStages = (projectId, payload) => {
    return fetchApi(`/projects/${projectId}/stages`, {
        method: 'PUT',
        body: JSON.stringify(payload),
    });
};

/**
 * Обновляет состав исполнителей и куратора для задачи.
 * @param {string} taskId ID задачи.
 * @param {object} payload Объект с curatorId и memberIds.
 * @returns {Promise<object>}
 */
export const updateTaskMembers = (taskId, payload) => {
    return fetchApi(`/tasks/${taskId}/members`, {
        method: 'PUT',
        body: JSON.stringify(payload),
    });
};
