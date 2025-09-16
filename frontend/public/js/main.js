import * as render from './ui/render.js';
import * as modals from './ui/modals.js';
import * as uiUtils from './ui/utils.js';
import * as auth from './auth.js';
import * as handlers from './handlers.js';
import * as store from './store.js';

function initializeApp() {
    const mainContainer = document.getElementById('main-content');
    
    // ОБРАБОТЧИК КЛИКОВ ДЛЯ ОСНОВНОГО КОНТЕНТА (ЗАДАЧИ, ПРОЕКТЫ И Т.Д.)
    mainContainer.addEventListener('click', async (event) => {
        // --- Обработчик клика по иконке статуса в карточке ---
        const statusActionArea = event.target.closest('.status-action-area');
        if (statusActionArea) {
            event.stopPropagation();
            const taskCard = statusActionArea.closest('[data-task-id]');
            modals.openStatusModal(taskCard.dataset.taskId);
            return;
        }

        // --- Обработчик кнопки "Редактировать" внутри карточки ---
        if (event.target.id === 'edit-task-btn') {
            event.stopPropagation();
            const detailsContainer = event.target.closest('.task-details');
            if (detailsContainer) {
                const backButtonHandler = () => {
                    uiUtils.exitEditMode(detailsContainer);
                    const { task } = store.findTask(detailsContainer.id.replace('task-details-', ''));
                    render.renderTaskDetails(detailsContainer, task, store.getAllUsers(), store.getAppData().userRole);
                    uiUtils.updateFabButtonUI(false, handlers.handleSaveActiveTask, handlers.handleShowAddTaskModal);
                };
                uiUtils.enterEditMode(detailsContainer, backButtonHandler);
                uiUtils.updateFabButtonUI(true, handlers.handleSaveActiveTask, handlers.handleShowAddTaskModal);
            }
            return;
        }

        // --- Обработчик полей, вызывающих модальные окна в режиме редактирования ---
        const editModeContainer = event.target.closest('.task-details.edit-mode');
        if (editModeContainer) {
            event.stopPropagation();
            const modalType = event.target.dataset.modalType;
            const activeTaskDetailsElement = editModeContainer;
            if (modalType === 'status') {
                const taskId = activeTaskDetailsElement.closest('[data-task-id]').dataset.taskId;
                modals.openStatusModal(taskId);
            } else if (modalType === 'user') {
                const appData = store.getAppData();
                modals.openUserModal(activeTaskDetailsElement, store.getAllUsers(), appData.userRole);
            } else if (modalType === 'project') {
                modals.openProjectModal(activeTaskDetailsElement, store.getAllProjects());
            } else if (modalType === 'stage') {
                modals.openStageModal(activeTaskDetailsElement);
            }
            return;
        }

        // --- Обработчик клика по заголовку ЗАДАЧИ ---
        const taskHeader = event.target.closest('.task-header');
        if (taskHeader) {
            // Игнорируем клик, если он был по области смены статуса
            if (event.target.closest('.status-action-area')) return;
            
            const detailsContainer = taskHeader.nextElementSibling;
            // Закрываем все остальные открытые задачи
            const currentlyOpen = document.querySelector('.task-details.expanded');
            if (currentlyOpen && currentlyOpen !== detailsContainer) {
                currentlyOpen.classList.remove('expanded');
                setTimeout(() => { if (currentlyOpen) currentlyOpen.innerHTML = ''; }, 300);
            }

            // Если контент пуст, рендерим его
            if (!detailsContainer.innerHTML) {
                const taskId = taskHeader.closest('.task').dataset.taskId;
                const { task } = store.findTask(taskId);
                const appData = store.getAppData();
                render.renderTaskDetails(detailsContainer, task, appData.allUsers, appData.userRole);
            }

            detailsContainer.classList.toggle('expanded');
            if (!detailsContainer.classList.contains('expanded')) {
                setTimeout(() => { if (detailsContainer) detailsContainer.innerHTML = ''; }, 300);
            }
            return;
        }

        // --- Обработчик кнопок управления проектом ---
        const manageMembersBtn = event.target.closest('.manage-members-btn');
        if (manageMembersBtn) {
            event.stopPropagation();
            handlers.handleManageMembers(manageMembersBtn.dataset.projectId, manageMembersBtn.dataset.projectName);
            return;
        }
        const manageStagesBtn = event.target.closest('.manage-stages-btn');
        if (manageStagesBtn) {
            event.stopPropagation();
            handlers.handleManageStages(manageStagesBtn.dataset.projectId, manageStagesBtn.dataset.projectName);
            return;
        }

        // --- Обработчик сворачивания/разворачивания проекта ---
        const projectHeader = event.target.closest('.project-header');
        if (projectHeader) {
            const projectElement = projectHeader.closest('.project-card');
            if (!projectElement) return;
            const projectBody = projectElement.querySelector('.project-content');
            if (!projectBody) return;
            projectBody.classList.toggle('expanded');
            return;
        }

        // --- Обработчик кнопки удаления задачи ---
        const deleteBtn = event.target.closest('.delete-btn');
        if (deleteBtn) {
            event.stopPropagation();
            const taskCard = deleteBtn.closest('[data-task-id]');
            handlers.handleDeleteTask(taskCard.dataset.taskId);
            return;
        }
    });

    const viewToolbar = document.getElementById('view-toolbar');
    viewToolbar.addEventListener('click', (event) => {
        const targetButton = event.target.closest('.view-btn');
        if (!targetButton) return;

        if (targetButton.id === 'view-btn-stages-filter') {
            modals.openStagesFilterModal(rerenderApp);
            return;
        }

        if (targetButton.closest('.view-modes')) {
            viewToolbar.querySelectorAll('.view-modes .view-btn').forEach(btn => btn.classList.remove('active'));
            targetButton.classList.add('active');
            rerenderApp();
        }
    });

    // --- ЛОГИКА DRAG AND DROP ---
    let draggedElement = null;
    mainContainer.addEventListener('dragstart', (e) => {
        if (document.querySelector('.task-details.edit-mode')) {
            e.preventDefault();
            return;
        }
        const draggableCard = e.target.closest('[draggable="true"]');
        if (!draggableCard) return;
        draggedElement = draggableCard;
        setTimeout(() => { if (draggedElement) draggedElement.classList.add('dragging'); }, 0);
    });

    function getDragAfterElement(container, y) {
        const draggableElements = [...container.querySelectorAll('[draggable="true"]:not(.dragging)')];
        return draggableElements.reduce((closest, child) => {
            const box = child.getBoundingClientRect();
            const offset = y - box.top - box.height / 2;
            if (offset < 0 && offset > closest.offset) {
                return { offset, element: child };
            }
            return closest;
        }, { offset: Number.NEGATIVE_INFINITY }).element;
    }
    mainContainer.addEventListener('dragover', (e) => {
        e.preventDefault();
        if (!draggedElement) return;
        const container = e.target.closest(`.tasks-list[data-status-group="${draggedElement.dataset.statusGroup}"]`);
        document.querySelectorAll('.drag-over, .drag-over-end').forEach(el => el.classList.remove('drag-over', 'drag-over-end'));
        if (!container) return;
        const afterElement = getDragAfterElement(container, e.clientY);
        if (afterElement) {
            afterElement.classList.add('drag-over');
        } else {
            container.classList.add('drag-over-end');
        }
    });
    mainContainer.addEventListener('drop', (e) => {
        e.preventDefault();
        if (!draggedElement) return;
        const dropContainer = e.target.closest(`.tasks-list[data-status-group="${draggedElement.dataset.statusGroup}"]`);
        if (!dropContainer) {
             mainContainer.dispatchEvent(new Event('dragend'));
             return;
        };
        const afterElement = getDragAfterElement(dropContainer, e.clientY);
        if (afterElement) {
            dropContainer.insertBefore(draggedElement, afterElement);
        } else {
            dropContainer.appendChild(draggedElement);
        }
        const appData = store.getAppData();
        const groupName = !['owner', 'admin'].includes(appData.userRole) 
            ? appData.userName 
            : (draggedElement.closest('.project-card')?.querySelector('.project-header h2')?.textContent || 'Unknown Project');
        const updatedTaskIds = Array.from(dropContainer.querySelectorAll('[draggable="true"]')).map(card => card.dataset.taskId);
        handlers.handleDragDrop(groupName, updatedTaskIds, appData.userRole);
    });
    mainContainer.addEventListener('dragend', () => {
        if (draggedElement) draggedElement.classList.remove('dragging');
        document.querySelectorAll('.drag-over, .drag-over-end').forEach(el => el.classList.remove('drag-over', 'drag-over-end'));
        draggedElement = null;
        uiUtils.showFab();
    });

    function rerenderApp() {
    const appData = store.getAppData();
    const activeFilters = store.getActiveStageFilters();
    const activeFiltersSet = new Set(activeFilters);

    const activeModeBtn = document.querySelector('.view-modes .view-btn.active');
    if (!activeModeBtn) return;

    let baseTasks = [];
    if (activeModeBtn.id === 'view-btn-my-tasks') {
        const allTasks = appData.projects.flatMap(p => p.tasks);
        const currentUserId = appData.currentUserId;
        baseTasks = allTasks.filter(task => 
            task.userId == currentUserId || 
            (task.members && task.members.some(member => member.userId == currentUserId))
        );
    } else {
        baseTasks = appData.projects.flatMap(p => p.tasks);
    }

    // --- ДОБАВЛЕНО: Логирование массива задач перед фильтрацией ---
    console.log('[MAIN] > Массив задач для фильтрации (baseTasks):', baseTasks);
    // ----------------------------------------------------------

    const filteredTasks = activeFilters.length > 0
        ? baseTasks.filter(task => activeFiltersSet.has(String(task.stageId)))
        : baseTasks;

    if (activeModeBtn.id === 'view-btn-projects') {
        const filteredProjects = JSON.parse(JSON.stringify(appData.projects));
        const filteredTasksSet = new Set(filteredTasks.map(t => t.taskId));
        filteredProjects.forEach(p => {
            p.tasks = p.tasks.filter(t => filteredTasksSet.has(t.taskId));
        });
        render.renderProjects(filteredProjects, appData.userName, appData.userRole, {});
    } else {
        render.renderTasksView(filteredTasks, store.getAllStatuses(), activeModeBtn.id === 'view-btn-my-tasks');
    }

    const filterBtn = document.getElementById('view-btn-stages-filter');
    if (activeFilters.length > 0) {
        filterBtn.classList.add('filtered');
    } else {
        filterBtn.classList.remove('filtered');
    }
}

    // --- Инициализация приложения ---
    auth.initializeApp().then(success => {
        if (!success) {
            console.error("Application initialization failed. Event listeners will not be attached.");
            return;
        }

        modals.setupModals(handlers.handleStatusUpdate);
        uiUtils.updateFabButtonUI(false, handlers.handleShowAddTaskModal, handlers.handleShowAddTaskModal);

        const toolbar = document.getElementById('view-toolbar');
        toolbar.classList.remove('hidden');
        toolbar.classList.add('flex');

        const userRole = store.getAppData().userRole;
        const allowedRolesForMyTasks = ['admin', 'owner', 'client'];
        if (allowedRolesForMyTasks.includes(userRole)) {
            document.getElementById('view-btn-my-tasks').classList.remove('hidden');
        }
        store.selectAllStagesByDefault(); // Выбираем все этапы по умолчанию
        rerenderApp(); // Сразу отрисовываем контент с учетом фильтров
    });
}

document.addEventListener('DOMContentLoaded', initializeApp);