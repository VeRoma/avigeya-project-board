// public/js/handlers.test.js

import { handleSaveActiveTask } from './handlers.js';
import * as api from './api.js';
import * as store from './store.js';
import * as uiUtils from './ui/utils.js';
import * as render from './ui/render.js';

// --- Создаем "заглушки" для всех внешних зависимостей ---
jest.mock('./api.js');
jest.mock('./store.js');
jest.mock('./ui/utils.js');
jest.mock('./ui/render.js');

// --- Имитируем объект Telegram Web App ---
beforeAll(() => {
    global.window = {
        Telegram: {
            WebApp: {
                // Добавим MainButton для полноты картины, т.к. она может использоваться
                MainButton: {
                    showProgress: jest.fn(),
                    hideProgress: jest.fn(),
                    setText: jest.fn(),
                    enable: jest.fn(),
                },
                // Используем jest.fn() для отслеживания вызовов
                showAlert: jest.fn(),
                HapticFeedback: {
                    notificationOccurred: jest.fn(),
                },
            },
        },
    };
});

// --- Описываем наш набор тестов ---
describe('handleSaveActiveTask', () => {    

    // Перед каждым тестом будем очищать все "заглушки" и готовить DOM
    beforeEach(() => {
        jest.clearAllMocks();
        
       
        const taskId = 'task-79';
        const initialTaskData = {
            taskId: taskId,
            name: 'Старое название',
            projectId: '1', // Используем ID
            curatorId: '10', // ID куратора
            members: [{ userId: '10', name: 'Роман' }], // Массив объектов
            status: { statusId: '1', name: 'К выполнению' }, // Объект статуса
            message: 'Старое сообщение',
            stageId: '1',
            version: 1
        };

        document.body.innerHTML = `
            <div id="main-content">
                <div class="card" data-task-id="${taskId}">
                    <div id="task-details-${taskId}" class="task-details edit-mode" data-version="1" data-task='${JSON.stringify(initialTaskData)}'>
                        <input type="text" class="task-name-edit" value="Новое название">
                        <textarea class="task-message-edit">Новое сообщение</textarea>
                        <p class="task-status-view" data-status-id="2">В работе</p>
                        <p class="task-project-view" data-project-id="2">Новый Проект</p>
                        <p class="task-responsible-view" data-member-ids="15,16">Александра, Максим</p>
                        <p class="task-stage-view" data-stage-id="3">Новый Этап</p>
                    </div>
                </div>
            </div>
            <button id="fab-button"></button>
        `;
    });

    // --- НАШ ГЛАВНЫЙ ТЕСТ ---
    test('должна корректно считывать все измененные поля и отправлять их на сервер', async () => {
        // 1. ГОТОВИМ ДАННЫЕ (Arrange)
        const taskId = 'task-79';
        const mockTaskInStore = {
            taskId: taskId,
            projectId: '1',
            version: 1
        };

        // Мокируем функции store для поиска по ID/имени
        store.findTask.mockReturnValue({ task: mockTaskInStore });
        store.getAppData.mockReturnValue({ userName: 'Admin' });
        store.findStatusByName.mockImplementation((name) => {
            const statuses = {
                'В работе': { statusId: '2', name: 'В работе' }
            };
            return statuses[name];
        });
        store.findUserById.mockImplementation((id) => {
            const users = {
                '15': { userId: '15', name: 'Александра' },
                '16': { userId: '16', name: 'Максим' }
            };
            return users[id];
        });

        // Мок для api.saveTask
        api.saveTask.mockResolvedValue({ status: 'success', newVersion: 2 });
        
        // 2. ВЫПОЛНЯЕМ ДЕЙСТВИЕ (Act)
        await handleSaveActiveTask();

        // 3. ПРОВЕРЯЕМ РЕЗУЛЬТАТ (Assert)

        // Убеждаемся, что функция сохранения была вызвана ровно один раз
        expect(api.saveTask).toHaveBeenCalledTimes(1);

        // Получаем данные, которые были отправлены в api.saveTask
        const sentPayload = api.saveTask.mock.calls[0][0];
        const sentTaskData = sentPayload.taskData;

        // Проверяем, что имя модификатора передано правильно
        expect(sentPayload.modifierName).toBe('Admin');

        // Проверяем каждое поле в отправленных данных
        expect(sentTaskData.taskId).toBe(taskId);
        expect(sentTaskData.name).toBe('Новое название');
        expect(sentTaskData.message).toBe('Новое сообщение');
        expect(sentTaskData.version).toBe(1); // Отправляем старую версию для проверки на сервере

        // Проверяем, что отправляются ID и объекты, как ожидает бэкенд
        expect(sentTaskData.projectId).toBe('2');
        expect(sentTaskData.stageId).toBe('3');
        expect(sentTaskData.status).toEqual({ statusId: '2', name: 'В работе' });

        // Проверяем, что куратор (первый в списке) и все участники отправляются как объекты
        const expectedMembers = [
            { userId: '15', name: 'Александра' },
            { userId: '16', name: 'Максим' }
        ];
        expect(sentTaskData.members).toEqual(expectedMembers);
        expect(sentTaskData.curator).toEqual(expectedMembers[0]); // Первый участник становится куратором

        // Проверяем, что после успешного сохранения были вызваны UI-функции
        expect(uiUtils.showMessage).toHaveBeenCalledWith('Изменения сохранены', 'success');
        expect(uiUtils.exitEditMode).toHaveBeenCalled();
        expect(render.renderProjects).toHaveBeenCalled();
    });
});
