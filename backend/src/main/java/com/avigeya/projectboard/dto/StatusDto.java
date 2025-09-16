package com.avigeya.projectboard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatusDto {
    private Long id;
    private String name;
    private String icon;
    private Integer order;

    public StatusDto(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}
