package com.legalbureau.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class LegalCaseResponseDto {
    private Long id;
    private String caseNumber;
    private String description;
    private String status;
    private LocalDateTime createdAt;

    private String clientFullName;
    private String lawyerFullName;
    private String categoryName;
}