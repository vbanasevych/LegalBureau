package com.legalbureau.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChartDataDto {
    private String label;
    private Long count;
}