package com.legalbureau.dto.response;

import lombok.Data;

@Data
public class UserResponseDto {
    private Long id;
    private String email;
    private String fullName;
    private String phone;
    private String role;
}
