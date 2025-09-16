package com.avigeya.projectboard.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "users") // Явно указываем имя таблицы "users"
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "name") // Указываем, что имя не может быть пустым
    private String name;

    private Long tgUserId;

    @Column(nullable = false)
    private String role;

    private String description;
}