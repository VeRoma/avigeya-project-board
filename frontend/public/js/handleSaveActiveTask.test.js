import { handleSaveActiveTask } from './handlers.js';
import * as api from './api.js';
import * as store from './store.js';
import * as uiUtils from './ui/utils.js';

// Mock dependencies
jest.mock('./api.js');
jest.mock('./store.js');
jest.mock('./ui/utils.js');

describe('handleSaveActiveTask', () => {

    beforeEach(() => {
        // Reset mocks before each test
        jest.clearAllMocks();
    });

    test('should call api.saveTask with correct data and update UI on success', async () => {
        // 1. Arrange
        const updatedTaskData = {
            id: '101',
            name: 'Updated Name',
            version: 1,
            // ... other properties
        };

        const apiResponse = {
            status: 'success',
            task: {
                id: '101',
                name: 'Updated Name',
                version: 2, // Version incremented by backend
            },
        };

        // Mock the API call to resolve successfully
        api.saveTask.mockResolvedValue(apiResponse);

        // Mock the DOM element for exitEditMode
        document.body.innerHTML = `<div class="task-details edit-mode" data-version="1"></div>`;
        const activeEditElement = document.querySelector('.task-details.edit-mode');

        // 2. Act
        const result = await handleSaveActiveTask(updatedTaskData);

        // 3. Assert
        expect(result).toBe(true); // Should return true on success
        expect(api.saveTask).toHaveBeenCalledWith(updatedTaskData);
        expect(api.saveTask).toHaveBeenCalledTimes(1);

        expect(uiUtils.showMessage).toHaveBeenCalledWith('Изменения сохранены', 'success');
        expect(store.updateTask).toHaveBeenCalledWith(apiResponse.task);

        // Check that UI update functions are called
        expect(uiUtils.exitEditMode).toHaveBeenCalled();
        expect(uiUtils.updateFabButtonUI).toHaveBeenCalledWith(false, null, expect.any(Function));
    });

});