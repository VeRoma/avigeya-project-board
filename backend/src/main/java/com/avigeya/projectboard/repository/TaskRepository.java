package com.avigeya.projectboard.repository;

import com.avigeya.projectboard.domain.Project;
import com.avigeya.projectboard.domain.Task;
import com.avigeya.projectboard.domain.User; // <-- Добавь импорт
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByProject(Project project);

    // НОВЫЙ МЕТОД:
    List<Task> findByCuratorOrAuthor(User curator, User author);
}