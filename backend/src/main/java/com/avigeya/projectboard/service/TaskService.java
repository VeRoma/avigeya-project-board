package com.avigeya.projectboard.service;

import com.avigeya.projectboard.domain.Task;
import com.avigeya.projectboard.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    @Autowired
    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    /**
     * Метод для получения всех задач из базы данных.
     * @return Список всех задач.
     */
    public List<Task> findAllTasks() {
        return taskRepository.findAll();
    }
}