package com.avigeya.projectboard.repository;

import com.avigeya.projectboard.domain.TaskMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TaskMemberRepository extends JpaRepository<TaskMember, Long> {
    @Modifying
    @Query("DELETE FROM TaskMember tm WHERE tm.task.id = :taskId")
    void deleteByTaskId(@Param("taskId") Long taskId);
}