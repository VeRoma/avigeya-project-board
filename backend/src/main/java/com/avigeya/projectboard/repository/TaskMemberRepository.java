package com.avigeya.projectboard.repository;

import com.avigeya.projectboard.domain.TaskMember;
import com.avigeya.projectboard.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskMemberRepository extends JpaRepository<TaskMember, Long> {
    List<TaskMember> findByUser(User user);
}