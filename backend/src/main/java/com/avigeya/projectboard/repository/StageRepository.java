package com.avigeya.projectboard.repository;

import com.avigeya.projectboard.domain.Stage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StageRepository extends JpaRepository<Stage, Long> {
}