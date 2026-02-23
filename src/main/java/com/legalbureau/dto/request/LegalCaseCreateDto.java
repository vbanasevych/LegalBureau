package com.legalbureau.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LegalCaseCreateDto {

    @NotBlank(message = "Опис справи не може бути порожнім")
    private String description;

    @NotNull(message = "ID клієнта обов'язкове")
    private Long clientId;

    @NotNull(message = "ID адвоката обов'язкове")
    private Long lawyerId;

    @NotNull(message = "ID категорії справи обов'язкове")
    private Long caseCategoryId;
}