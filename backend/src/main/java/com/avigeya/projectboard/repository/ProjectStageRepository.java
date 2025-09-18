package com.avigeya.projectboard.repository;

import com.avigeya.projectboard.domain.ProjectStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectStageRepository extends JpaRepository<ProjectStage, Long> {

    @Modifying
    @Query("DELETE FROM ProjectStage ps WHERE ps.project.id = :projectId")
    void deleteByProjectId(Long projectId);
}