package com.avigeya.projectboard.repository;

import com.avigeya.projectboard.domain.ProjectMember;
import com.avigeya.projectboard.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {
    List<ProjectMember> findByUser(User user);
}