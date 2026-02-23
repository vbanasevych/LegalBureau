package com.legalbureau.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class LawyerResponseDto {
    private Long userId;
    private String email;
    private String fullName;
    private String phone;
    private BigDecimal hourlyRate;
    private String city;
    private String bio;
    private List<String> specializations;
}
