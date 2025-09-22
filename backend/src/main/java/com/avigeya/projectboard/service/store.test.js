import * as store from './store.js';

// Mock-данные для тестов
const mockAppData = {
    projects: [
        {
            id: 1,
            name: 'Project Alpha',
            tasks: [
                { id: 101, name: 'Task A', projectId: 1, version: 0 },
                { id: 102, name: 'Task B', projectId: 1, version: 2 },
            ],
        },
    ],
    allProjects: [{ id: 1, name: 'Project Alpha' }],
    allUsers: [],
    allStatuses: [],
};


describe('store.js', () => {

    // Перед каждым тестом сбрасываем состояние хранилища
    beforeEach(() => {
        // Создаем глубокую копию, чтобы тесты не влияли друг на друга
        const freshMockData = JSON.parse(JSON.stringify(mockAppData));
        store.setAppData(freshMockData);
    });

    test('setAppData should correctly initialize the store', () => {
        const appData = store.getAppData();
        expect(appData).toEqual(mockAppData);
        expect(store.getAllProjects()).toEqual(mockAppData.allProjects);
    });

    test('findTask should return the correct task and project', () => {
        const { task, project } = store.findTask(101);
        expect(task).not.toBeNull();
        expect(task.name).toBe('Task A');
        expect(project).not.toBeNull();
        expect(project.name).toBe('Project Alpha');
    });

    test('findTask should return null for a non-existent task', () => {
        const { task, project } = store.findTask(999);
        expect(task).toBeNull();
        expect(project).toBeNull();
    });

    test('updateTask should correctly update task properties', () => {
        const updatedData = {
            id: 101,
            name: 'Task A - Updated',
            version: 1,
        };
        store.updateTask(updatedData);

        const { task } = store.findTask(101);
        expect(task.name).toBe('Task A - Updated');
        expect(task.version).toBe(1);
    });

});