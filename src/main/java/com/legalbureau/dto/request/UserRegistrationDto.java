package com.legalbureau.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRegistrationDto {

    @NotBlank(message = "Email не може бути порожнім")
    @Email(message = "Неправильний формат email")
    private String email;

    @NotBlank(message = "Пароль не може бути порожнім")
    @Size(min = 6, message = "Пароль має містити щонайменше 6 символів")
    private String password;

    @NotBlank(message = "ПІБ обов'язкове")
    @Size(max = 100, message = "ПІБ не може перевищувати 100 символів")
    private String fullName;

    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Неправильний формат номеру телефону")
    private String phone;

    @NotBlank(message = "Роль обов'язкова")
    @Pattern(regexp = "^(ADMIN|CLIENT|LAWYER)$", message = "Роль має бути ADMIN, CLIENT або LAWYER")
    private String role;
}