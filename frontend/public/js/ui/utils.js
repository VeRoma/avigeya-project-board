import * as store from '../store.js';
import * as handlers from '../handlers.js';

const toast = document.getElementById('toast-notification');
const fabButton = document.getElementById('fab-button');
const fabIconContainer = document.getElementById('fab-icon-container');
const mainContainer = document.getElementById('main-content');
const loadingOverlay = document.getElementById('loading-overlay');
const app = document.getElementById('app'); // Добавляем недостающую константу

const ICONS = {
    refresh: `<svg class="h-7 w-7" fill="none" viewBox="0 0 24 24" stroke="var(--tg-theme-button-text-color, #ffffff)" stroke-width="2"><path d="M21 12a9 9 0 1 1-6.219-8.56" stroke-linecap="round" stroke-linejoin="round"></path></svg>`,
    save: `<svg class="h-7 w-7" fill="none" viewBox="0 0 24 24" stroke="var(--tg-theme-button-text-color, #ffffff)" stroke-width="2"><path d="M8 7H5a2 2 0 00-2 2v9a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2h-3m-1 4l-3 3m0 0l-3-3m3 3V4" stroke-linecap="round" stroke-linejoin="round"></path></svg>`,
    add: `<svg class="h-8 w-8" fill="none" viewBox="0 0 24 24" stroke="var(--tg-theme-button-text-color, #ffffff)" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M12 4v16m8-8H4"></path></svg>`
};

let saveTimeout;
let currentFabClickHandler = null; // Переменная для хранения текущего обработчика
let currentBackButtonHandler = null; // Переменная для хранения обработчика кнопки "Назад"

export function showToast(message, type = 'info') {
    toast.textContent = message;
    toast.classList.remove('success', 'info'); // Сбрасываем предыдущие классы типа
    toast.classList.add(type === 'success' ? 'success' : 'info');

    toast.classList.add('show');
    clearTimeout(saveTimeout);
    saveTimeout = setTimeout(() => {
        toast.classList.remove('show');
    }, 2500);
}

export function showLoading() {
    if (app) app.classList.remove('hidden');
    if (mainContainer) {
        mainContainer.innerHTML = ''; // Очищаем контейнер
        const loadingContainer = document.createElement('div');
        loadingContainer.className = 'text-center py-10';
        loadingContainer.innerHTML = '<div class="animate-spin rounded-full h-8 w-8 border-b-2 border-gray-500 mx-auto mt-4"></div>';
        mainContainer.appendChild(loadingContainer);
    }
}

export function hideLoading() {
    if (loadingOverlay) loadingOverlay.style.display = 'none';
}

export function showDataLoadError(error) {
    if (!mainContainer) return;
    const errorMessage = typeof error === 'object' ? error.message : String(error);
    mainContainer.innerHTML = ''; // Очищаем
    mainContainer.innerHTML = `<div class="p-4 bg-red-100 text-red-700 rounded-lg"><p class="font-bold">Ошибка загрузки</p><p class="text-sm mt-1"></p></div>`;
    mainContainer.querySelector('.text-sm').textContent = errorMessage;
}

export function updateFabButtonUI(isEditMode, saveHandler, addHandler) {
    // Корректно удаляем предыдущий обработчик, чтобы избежать утечек памяти и двойных срабатываний
    if (currentFabClickHandler) {
        fabButton.removeEventListener('click', currentFabClickHandler);
    }
    
    if (isEditMode) {
        // Настраиваем кнопку для режима "Сохранить"
        fabIconContainer.innerHTML = ICONS.save;
        currentFabClickHandler = saveHandler;
    } else {
        // Настраиваем кнопку для обычного режима "Добавить"
        fabIconContainer.innerHTML = ICONS.add;
        currentFabClickHandler = addHandler;
    }
    // Привязываем новый актуальный обработчик
    fabButton.addEventListener('click', currentFabClickHandler);
}

export function showAccessDeniedScreen() {
    if (app) app.classList.add('hidden');
    document.getElementById('auth-blocker').classList.remove('hidden');
}

export function showRegistrationModal() {
    document.body.classList.add('overflow-hidden');
    document.getElementById('app').classList.add('hidden');
    
    // --- ИСПРАВЛЕНИЕ ЗДЕСЬ ---
    // Делаем видимым родительский контейнер
    document.getElementById('auth-blocker').classList.remove('hidden');
    // -------------------------

    document.getElementById('registration-modal').classList.add('active');
}

export function setupUserInfo(nameFromSheet) {
    const greetingElement = document.getElementById('greeting-text');
    const userIdElement = document.getElementById('user-id-text');
    
    // Используем имя, полученное с сервера (из таблицы Users)
    const displayName = nameFromSheet || 'Пользователь';
    greetingElement.textContent = `Привет, ${displayName}!`;

    // Просто скрываем элемент, где отображался ID
    if (userIdElement) {
        userIdElement.style.display = 'none';
    }
}

export function hideFab() {
    if(fabButton) fabButton.classList.add('hidden');
}

export function showFab() {
    if(fabButton) fabButton.classList.remove('hidden');
}

export function setBackButtonHandler(handler) {
    currentBackButtonHandler = handler;
}

export function getBackButtonHandler() {
    return currentBackButtonHandler;
}

export function enterEditMode(detailsContainer, onBackCallback) {
    const tg = window.Telegram.WebApp;
    detailsContainer.classList.add('edit-mode');
    tg.BackButton.onClick(onBackCallback);
    tg.BackButton.show();
}

export function exitEditMode(detailsContainer, onBackCallback) {
    const tg = window.Telegram.WebApp;
    if (detailsContainer) {
        detailsContainer.classList.remove('edit-mode');
    }
    tg.BackButton.hide();
    if (onBackCallback) tg.BackButton.offClick(onBackCallback);
}

/**
 * Показывает кастомное уведомление вверху экрана.
 * @param {string} message - Сообщение для отображения.
 * @param {string} type - Тип уведомления ('success' или 'error').
 */
export function showNotification(message, type = 'success') {
    // Удаляем старое уведомление, если оно есть
    const oldNotification = document.querySelector('.notification');
    if (oldNotification) {
        oldNotification.remove();
    }

    // Создаем новый элемент уведомления
    const notification = document.createElement('div');
    notification.className = `notification ${type}`;
    notification.textContent = message;
    document.body.appendChild(notification);

    // Показываем уведомление
    setTimeout(() => {
        notification.classList.add('show');
    }, 10); // Небольшая задержка для срабатывания CSS-анимации

    // Скрываем и удаляем уведомление через 3 секунды
    setTimeout(() => {
        notification.classList.remove('show');
        setTimeout(() => {
            notification.remove();
        }, 500); // Ждем окончания анимации скрытия
    }, 3000);
}

/**
 * Универсальная функция для отображения уведомлений пользователю.
 * @param {string} message - Текст сообщения.
 * @param {string} type - Тип сообщения ('success', 'error', 'info'). По умолчанию 'info'.
 */
export function showMessage(message, type = 'info') {
    // Определяем цвет в зависимости от типа сообщения
    let notificationType = 'success'; // По умолчанию зеленый
    if (type === 'error') {
        notificationType = 'error'; // Красный для ошибок
    }
    // Для 'info' можно добавить отдельный стиль, но пока используем 'success'
    
    showNotification(message, notificationType);

    // Добавляем тактильный отклик для важных сообщений
    if (window.Telegram.WebApp.HapticFeedback) {
        if (type === 'success') {
            window.Telegram.WebApp.HapticFeedback.notificationOccurred('success');
        } else if (type === 'error') {
            window.Telegram.WebApp.HapticFeedback.notificationOccurred('error');
        }
    }
    
    // Также выводим ошибки в консоль для удобства отладки
    if (type === 'error') {
        console.error("App Message (Error):", message);
    } else {
        console.log("App Message (Info/Success):", message);
    }
}

/**
 * Получает текущее состояние (свернут/развернут) всех проектов на странице.
 * @returns {Object} Объект, где ключ - название проекта, значение - true (развернут) или false (свернут).
 */
export function getAccordionState() {
    const state = {};
    document.querySelectorAll('#main-content .card').forEach(card => {
        const projectNameElement = card.querySelector('.project-header h2');
        if (projectNameElement) {
            const projectName = projectNameElement.textContent;
            const content = card.querySelector('.collapsible-content');
            // Проверяем, есть ли у контента класс .expanded
            const isExpanded = content && content.classList.contains('expanded');
            state[projectName] = isExpanded;
        }
    });
    return state;
}

/**
 * Принудительно сворачивает все открытые карточки задач и очищает их содержимое.
 */
export function collapseAllTaskDetails() {
    document.querySelectorAll('.task-details.expanded').forEach(detailsContainer => {
        detailsContainer.classList.remove('expanded');
        // Очищаем содержимое, чтобы убрать "остаточную" верхушку
        detailsContainer.innerHTML = ''; 
    });
}

/**
 * Сравнивает оригинальные данные задачи с текущими данными из формы редактирования.
 * @param {object} originalTask - Оригинальный объект задачи из store.
 * @param {object} currentData - Объект с текущими данными из DOM.
 * @returns {boolean} - true, если есть изменения, иначе false.
 */
function haveChanges(originalTask, currentData) {
    if (!originalTask) return false;

    // Простое сравнение строковых и числовых полей
    if (originalTask.name !== currentData.name) return true;
    if ((originalTask.message || '') !== (currentData.message || '')) return true;
    if (String(originalTask.projectId) !== String(currentData.projectId)) return true;
    if (originalTask.stage && String(originalTask.stage.id) !== String(currentData.stageId)) return true;
    if (originalTask.status && String(originalTask.status.id) !== String(currentData.status.id)) return true;

    // Сравнение участников
    const originalMemberIds = new Set((originalTask.members || []).map(m => String(m.id)));
    const currentMemberIds = new Set((currentData.members || []).map(m => String(m.id)));
    if (originalMemberIds.size !== currentMemberIds.size) return true;
    for (const id of originalMemberIds) {
        if (!currentMemberIds.has(id)) return true;
    }

    return false;
}

/**
 * Собирает текущие данные из полей редактирования активной задачи.
 * @param {HTMLElement} activeEditElement - DOM-элемент контейнера .task-details.edit-mode.
 * @returns {object | null} - Объект с данными задачи или null, если элемент не найден.
 */
export function getCurrentTaskDataFromDOM(activeEditElement) {
    if (!activeEditElement) return null;

    const taskId = activeEditElement.id.replace('task-details-', '');
    const { task: originalTask } = store.findTask(taskId);
    if (!originalTask) return null;

    const memberIds = (activeEditElement.querySelector('.task-responsible-view').dataset.memberIds || '').split(',').filter(Boolean);
    const members = store.getUsersByIds(memberIds);

    // --- ЛОГ: Проверяем, что считывается из data-атрибута ---
    console.log(`[UTILS] > getCurrentTaskDataFromDOM: Считывание data-version="${activeEditElement.dataset.version}"`);

    return {
        id: taskId,
        name: activeEditElement.querySelector('.task-name-edit').value,
        message: activeEditElement.querySelector('.task-message-edit').value,
        projectId: activeEditElement.querySelector('.task-project-view').dataset.projectId,
        stageId: activeEditElement.querySelector('.task-stage-view').dataset.stageId,
        status: store.findStatusByName(activeEditElement.querySelector('.task-status-view').textContent),
        curator: members.length > 0 ? members[0] : null,
        members: members,
        version: parseInt(activeEditElement.dataset.version, 10),
        author: originalTask.author
    };
}


/**
 * Проверяет наличие несохраненных изменений в активной задаче.
 * Если изменения есть, предлагает пользователю сохранить их.
 * @param {function} proceedCallback - Функция, которая будет вызвана, если можно продолжить действие (нет изменений или пользователь сделал выбор).
 */
export function checkForUnsavedChanges(proceedCallback) {
    const tg = window.Telegram.WebApp;
    const activeEditElement = document.querySelector('.task-details.edit-mode');
    if (!activeEditElement) {
        proceedCallback();
        return;
    }

    const taskId = activeEditElement.id.replace('task-details-', '');
    const { task: originalTask } = store.findTask(taskId);

    // Используем новую универсальную функцию для сбора данных
    const currentData = getCurrentTaskDataFromDOM(activeEditElement);

    if (!currentData || !haveChanges(originalTask, currentData)) {
        proceedCallback();
        return;
    }

    const handleConfirmation = (confirmed) => {
        if (confirmed) {
            // Пользователь нажал "Сохранить"
            handlers.handleSaveActiveTask(currentData)
                .then(success => {
                    if (success) {
                        // После успешного сохранения выполняем отложенное действие
                        proceedCallback();
                    }
                    // Если сохранение не удалось, остаемся на месте, ошибка уже показана в handleSaveActiveTask
                });
        } else {
            // Пользователь нажал "Не сохранять"
            // Просто выходим из режима редактирования и выполняем отложенное действие
            const backButtonHandler = getBackButtonHandler();
            exitEditMode(activeEditElement, backButtonHandler);
            proceedCallback();
        }
    };

    // --- ИСПРАВЛЕНИЕ: Проверяем поддержку tg.showConfirm и используем window.confirm как запасной вариант для десктопа ---
    // Оборачиваем вызов в try-catch, так как в некоторых версиях сам факт обращения к tg.showConfirm может вызывать ошибку, если его нет.
    try {
        // Попытка использовать нативный метод Telegram
        tg.showConfirm("У вас есть несохраненные изменения. Сохранить их?", handleConfirmation);
    } catch (e) {
        // Если нативный метод не поддерживается, используем стандартный браузерный confirm
        const userConfirmed = window.confirm("У вас есть несохраненные изменения. Сохранить их?");
        handleConfirmation(userConfirmed);
    }
}