package com.legalbureau.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class CaseServiceCreateDto {

    @NotNull(message = "ID справи обов'язкове")
    private Long legalCaseId;

    @NotBlank(message = "Назва послуги обов'язкова")
    private String name;

    @NotNull(message = "Ціна обов'язкова")
    @DecimalMin(value = "0.01", message = "Ціна має бути більшою за 0")
    private BigDecimal price;
}