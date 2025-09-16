package com.avigeya.projectboard.repository;

import com.avigeya.projectboard.domain.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {
}