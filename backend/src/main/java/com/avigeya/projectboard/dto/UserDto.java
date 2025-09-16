package com.avigeya.projectboard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long id;
    private String name;
    private String role;

    public UserDto(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}
