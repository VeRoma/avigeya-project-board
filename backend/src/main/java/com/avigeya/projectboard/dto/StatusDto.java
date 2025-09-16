package com.avigeya.projectboard.dto;

import lombok.Data;

@Data
public class StatusDto {
    private Long statusId;
    private String name;
    private String icon;
    private Integer order;
}