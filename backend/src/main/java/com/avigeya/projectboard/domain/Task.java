package com.avigeya.projectboard.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

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

    @Column(columnDefinition = "TEXT")
    private String message;

    private Integer priority;

    private LocalDate startDate;

    private LocalDate finishDate;

    private boolean isDeleted;

    @Version
    private Integer version;

    // --- СВЯЗИ С ДРУГИМИ СУЩНОСТЯМИ ---

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id")
    private Status status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stage_id")
    private Stage stage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false) // Ответственный
    private User curator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_user_id", nullable = false) // Автор
    private User author;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TaskMember> taskMembers = new HashSet<>();

    /**
     * Вспомогательный метод для получения списка участников (User) из коллекции
     * связей (TaskMember).
     * Не является полем в базе данных.
     * 
     * @return Набор пользователей, являющихся участниками задачи.
     */
    @Transient // Указывает, что это поле не должно быть сохранено в БД
    public Set<User> getMembers() {
        return this.taskMembers.stream().map(TaskMember::getUser).collect(Collectors.toSet());
    }
}
