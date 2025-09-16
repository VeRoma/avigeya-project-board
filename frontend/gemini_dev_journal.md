Technical Specification for Frontend Migration to New Java/Spring Backend
Primary Goal:
Adapt the existing frontend application to work with the new Java/Spring backend. The main change is switching from multiple data-fetching endpoints to a single, powerful endpoint GET /api/initial-data. This endpoint provides all necessary data for the initial application load, tailored to the specific user.

File: public/js/api.js
Task: Refactor API communication layer.

Remove Obsolete Functions:

Find and completely remove or comment out the old data-fetching functions. These likely include fetchProjects(), fetchTasks(), fetchUsers(), fetchStatuses(), etc. They are no longer needed.

Create a New Master Function:

Create a new asynchronous function named fetchInitialData.

This function should accept one argument: userId.

Inside the function, perform a fetch request to the new backend endpoint. The URL should be constructed as /api/initial-data?userId=${userId}.

The function must handle the response: check if it's ok, parse the JSON, and return the resulting data object.

Include error handling (e.g., using a try...catch block) to log any issues during the fetch operation.

Export this function so it can be used by other modules.

JavaScript

// api.js - Example of the new function
/*
export async function fetchInitialData(userId) {
    try {
        const response = await fetch(`/api/initial-data?userId=${userId}`);
        if (!response.ok) {
            throw new Error('Network response was not ok');
        }
        return await response.json();
    } catch (error) {
        console.error("Failed to fetch initial data:", error);
        // Optionally, render an error message to the user
        return null;
    }
}
*/
File: public/js/store.js
Task: Adapt the state management to the new data structure.

Modify State Structure:

The global store object should be updated to reflect the new data structure. It should contain keys like currentUser, projects, and tasks. Initialize them as null or empty arrays/objects.

Update Data Initialization Logic:

Create or refactor a function, let's call it initializeStore.

This function should be asynchronous.

Inside initializeStore, call the new api.fetchInitialData(userId) function. For now, you can hardcode a userId (e.g., 1 for an admin) for testing purposes.

When the data is received from the API, populate the store with it.

store.currentUser = data.currentUser;

store.projects = data.projects;

store.tasks = data.tasks;

The backend now pre-filters tasks. Any client-side logic that filters out "completed" tasks on initial load can be removed, as the store.tasks array already contains only active tasks.

JavaScript

// store.js - Example of the updated structure and initialization
/*
import { fetchInitialData } from './api.js';

export const store = {
    currentUser: null,
    projects: [],
    tasks: [],
    // other state properties...
};

export async function initializeStore(userId) {
    const data = await fetchInitialData(userId);
    if (data) {
        store.currentUser = data.currentUser;
        store.projects = data.projects;
        store.tasks = data.tasks;
    }
    // The store is now ready.
}
*/
File: public/js/main.js
Task: Update the application's entry point.

Modify Initialization Sequence:

The main logic that runs when the page loads should now call store.initializeStore(userId).

Since initializeStore is asynchronous, you must use await or .then() to ensure that the rendering functions (like renderAll) are called after the data has been loaded into the store.

JavaScript

// main.js - Example of the new initialization flow
/*
import { initializeStore } from './store.js';
import { renderAll } from './ui/render.js';

document.addEventListener('DOMContentLoaded', async () => {
    const userId = 1; // Temporary: get this from auth later
    await initializeStore(userId);
    renderAll();
    // setup event handlers...
});
*/
File: public/js/ui/render.js
Task: Adapt rendering functions to the new DTO structure.

Update Data Access Paths:

Review all rendering functions (renderTasks, etc.).

The data source is now the global store object.

The structure of the task object has changed. It's now a TaskDto. Accessing related data will be different. For example:

Instead of finding a status name from a separate statuses array, you will now access it directly: task.status.name.

Instead of finding a curator's name, you will use: task.curator.name.

The project ID is now a direct property: task.projectId.

Implement Role-Based Rendering:

Use the store.currentUser.role property to conditionally render UI elements.

For example, in the function that renders the main interface, add a check:

JavaScript

// render.js - Example of role-based UI
/*
if (store.currentUser.role === 'owner' || store.currentUser.role === 'admin') {
    // Create and show the "Add New Project" or "Add New Task" button.
    const addButton = document.createElement('button');
    addButton.textContent = 'Add Task';
    // ... append it to the DOM
}
*/
File: public/js/handlers.js
Task: Implement role-based access control for user actions.

Add Permission Checks:

In event handlers for actions like creating, editing, or deleting items, add a check for the user's role before executing the logic.

This prevents users without the proper permissions from performing actions, even if the UI element was somehow visible.

JavaScript

// handlers.js - Example of a permission check
/*
function handleCreateTaskClick() {
    if (store.currentUser.role !== 'owner' && store.currentUser.role !== 'admin') {
        alert('You do not have permission to perform this action.');
        return;
    }
    // If permission is granted, proceed to show the modal or form.
    // ...
}
*/