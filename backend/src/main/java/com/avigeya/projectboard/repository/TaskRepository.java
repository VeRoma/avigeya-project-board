package com.avigeya.projectboard.repository;

import com.avigeya.projectboard.domain.Project;
import com.avigeya.projectboard.domain.Task;
import com.avigeya.projectboard.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface TaskRepository extends JpaRepository<Task, Long> {

       List<Task> findByProject(Project project);

       @Query("SELECT t FROM Task t " +
                     "LEFT JOIN FETCH t.stage " +
                     "LEFT JOIN FETCH t.project " +
                     "LEFT JOIN FETCH t.curator " +
                     "LEFT JOIN FETCH t.author " +
                     "LEFT JOIN FETCH t.status")
       Set<Task> findAllWithDetails();

       @Query("SELECT t FROM Task t " +
                     "LEFT JOIN FETCH t.stage LEFT JOIN FETCH t.project LEFT JOIN FETCH t.curator " +
                     "LEFT JOIN FETCH t.author LEFT JOIN FETCH t.status " +
                     "WHERE t.curator = :user OR t.author = :user OR " +
                     "EXISTS (SELECT tm FROM TaskMember tm WHERE tm.task = t AND tm.user = :user)")
       Set<Task> findTasksForUserWithDetails(@Param("user") User user);
}