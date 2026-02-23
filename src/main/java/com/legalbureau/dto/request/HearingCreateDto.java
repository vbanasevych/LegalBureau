package com.legalbureau.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class HearingCreateDto {

    @NotNull(message = "ID справи обов'язкове")
    private Long legalCaseId;

    @NotNull(message = "Дата засідання обов'язкова")
    @FutureOrPresent(message = "Дата засідання не може бути в минулому")
    private LocalDateTime hearingDate;

    @NotBlank(message = "Місце проведення обов'язкове")
    private String place;
}