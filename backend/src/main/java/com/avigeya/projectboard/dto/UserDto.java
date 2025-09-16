package com.avigeya.projectboard.dto;

import lombok.Data;

@Data // Аннотация Lombok, которая заменяет @Getter, @Setter, @ToString и др.
public class UserDto {
    private Long id;
    private String name;
}