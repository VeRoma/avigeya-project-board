import * as api from './api.js';
import * as render from './ui/render.js';
import * as uiUtils from './ui/utils.js';
import * as store from './store.js';
import * as handlers from './handlers.js';

/**
 * Получает ID пользователя для отладки из URL (например, ?debug_user_id=1).
 * @returns {string|null}
 */
function getDebugPayload() {
    const urlParams = new URLSearchParams(window.location.search);
    const userId = urlParams.get('debug_user_id');
    if (userId) {
        // Явно преобразуем ID в число, чтобы соответствовать Long на бэкенде
        return { debugUserId: parseInt(userId, 10) };
    }
    // В рабочем режиме возвращаем initData от Telegram
    return { initData: window.Telegram.WebApp.initData };
}

/**
 * Основная функция инициализации приложения.
 * Запрашивает данные с бэкенда, используя initData от Telegram или debug_user_id.
 */
export async function initializeApp() {
    console.log('[AUTH] > Начало инициализации приложения...');
    const tg = window.Telegram.WebApp;

    try {
        tg.ready();
        uiUtils.showLoading();

        console.log('[AUTH] > Шаг 1: Получение payload для аутентификации.');
        // 1. Получаем initData от Telegram или ID для отладки
        const payload = getDebugPayload();
        console.log('[AUTH] > Payload для бэкенда:', payload);

        // 2. Отправляем данные на наш Java-бэкенд для получения всех данных
        const appData = await api.getAppData(payload);
        // --- ДОБАВЛЕНО: Логирование полученных данных ---
        console.log('[AUTH] > Получены данные с сервера (appData):', appData);
        // ---------------------------------------------

        // --- START: KEY CORRECTION - Distribute tasks into projects ---
        // Бэкенд возвращает проекты и задачи раздельно. Соберем их вместе.
        if (appData && appData.projects && appData.tasks) {
            // 1. Создаем карту для быстрого доступа к проектам по ID
            const projectMap = new Map();
            appData.projects.forEach(p => projectMap.set(p.id, p));
            // 2. Инициализируем у каждого проекта пустой массив задач
            projectMap.forEach(p => {
                p.tasks = [];
            });

            // 3. Distribute each task into its corresponding project's tasks array.
            for (const task of appData.tasks) {
                const project = projectMap.get(task.projectId);
                if (project) {
                    project.tasks.push(task);
                }
            }
        }
        // --- END: KEY CORRECTION ---

        // 3. Сохраняем полученные данные в локальное хранилище (store)
        store.setAppData(appData);

        // 4. Настраиваем UI с полученными данными
        uiUtils.setupUserInfo(appData.userName);
        uiUtils.hideLoading();
        document.getElementById('app').classList.remove('hidden');

        // 5. Отрисовываем проекты
        const accordionState = {}; // Начальное состояние - все свернуто
        render.renderProjects(appData.projects, appData.userName, appData.userRole, accordionState, store.getStageFilters());

        // 7. Показываем основную кнопку Telegram
        tg.MainButton.setText('Новая задача');
        tg.MainButton.show();

        return true; // Сигнализируем, что инициализация прошла успешно
    } catch (error) {
        console.error('Critical initialization error:', error);
        if (error.message.includes('403') || error.message.toLowerCase().includes('access denied')) {
            uiUtils.showAccessDeniedScreen();
        } else {
            uiUtils.showDataLoadError(error);
        }
        tg.MainButton.hide();
        uiUtils.hideFab();
        uiUtils.hideLoading();
        return false; // Инициализация не удалась
    }
}