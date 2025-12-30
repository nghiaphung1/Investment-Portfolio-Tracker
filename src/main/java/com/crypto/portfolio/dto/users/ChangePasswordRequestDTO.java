package com.crypto.portfolio.dto.users;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordRequestDTO {

    @NotBlank(message = "OLD_PASSWORD_SHOULD_NOT_BE_NULL")
    private String oldPassword;

    @NotBlank(message = "NEW_PASSWORD_SHOULD_NOT_BE_NULL")
    @Size(min = 8, message = "PASSWORD_SHOULD_BE_AT_LEAST_8_CHARACTERS")
    private String newPassword;

    @NotBlank(message = "CONFIRM_PASSWORD_SHOULD_NOT_BE_NULL")
    private String confirmPassword;
}