package com.crypto.portfolio.domain.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRequestDTO {
    @NotBlank(message = "EMAIL_SHOULD_NOT_BE_NULL")
    @Email(message = "INVALID_EMAIL_FORMAT")
    private String email;

    @NotBlank(message = "PASSWORD_SHOULD_NOT_BE_NULL")
    private String password;

    @NotBlank(message = "NAME_SHOUT_NOT_BE_NULL")
    private String fullName;
}