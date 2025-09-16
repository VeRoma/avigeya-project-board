package com.avigeya.projectboard.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private Integer priority;

    private LocalDate startDate;

    private LocalDate finishDate;


    private boolean isDeleted;

    // --- СВЯЗИ С ДРУГИМИ СУЩНОСТЯМИ ---

    @ManyToOne
    @JoinColumn(name = "status_id") // Указываем, какой столбец используется для связи
    private Status status;

    @ManyToOne
    @JoinColumn(name = "stage_id")
    private Stage stage;


    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false) // Ответственный
    private User curator;

    @ManyToOne
    @JoinColumn(name = "author_user_id", nullable = false) // Автор
    private User author;

    // Пока пропустим status_id и stage_id, мы вернемся к ним позже
    // т.к. это тоже будут отдельные сущности-справочники.
}