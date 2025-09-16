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
        // Возвращаем объект, который будет отправлен на бэкенд
        return { debugUserId: userId };
    }
    // В рабочем режиме возвращаем initData от Telegram
    return { initData: window.Telegram.WebApp.initData };
}

/**
 * Основная функция инициализации приложения.
 * Запрашивает данные с бэкенда, используя initData от Telegram или debug_user_id.
 */
export async function initializeApp() {
    const tg = window.Telegram.WebApp;

    try {
        tg.ready();
        uiUtils.showLoading();

        // 1. Получаем initData от Telegram или ID для отладки
        const payload = getDebugPayload();

        // 2. Отправляем данные на наш Java-бэкенд для получения всех данных
        const appData = await api.getAppData(payload);

        // 3. Сохраняем полученные данные в локальное хранилище (store)
        store.setAppData(appData);

        // 4. Настраиваем UI с полученными данными
        uiUtils.setupUserInfo(appData.userName);
        uiUtils.hideLoading();
        document.getElementById('app').classList.remove('hidden');

        // 5. Отрисовываем проекты
        const accordionState = {}; // Начальное состояние - все свернуто
        render.renderProjects(appData.projects, appData.userName, appData.userRole, accordionState, store.getStageFilters());

        // 6. Запускаем фоновую загрузку дополнительных данных (связей)
        handlers.handleBackgroundDataFetch();

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