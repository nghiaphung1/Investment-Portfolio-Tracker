package com.crypto.portfolio.dto.otp;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResendOtpRequestDTO {
    @NotNull(message = "USER_ID_SHOULD_NOT_BE_NULL")
    private Long userId;
}
