package com.avigeya.projectboard.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "project_stages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectStage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stage_id", nullable = false)
    private Stage stage;

    @Column(name = "is_active")
    private Boolean isActive;

    // Конструктор для удобного создания без ID
    public ProjectStage(Project project, Stage stage, Boolean isActive) {
        this.project = project;
        this.stage = stage;
        this.isActive = isActive;
    }
}