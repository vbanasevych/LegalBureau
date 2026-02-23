package com.legalbureau.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Set;

@Data
public class LawyerProfileUpdateDto {

    @Min(value = 0, message = "Погодинна ставка не може бути від'ємною")
    private BigDecimal hourlyRate;

    @Size(max = 100, message = "Назва міста занадто довга")
    private String city;

    private String bio;
    private Set<Long> categoryIds;
}