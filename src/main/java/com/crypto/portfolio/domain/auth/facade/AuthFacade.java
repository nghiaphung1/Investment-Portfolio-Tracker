package com.crypto.portfolio.domain.auth.facade;

import com.crypto.portfolio.domain.auth.dto.RegisterRequestDTO;
import com.crypto.portfolio.domain.user.dto.UserResponseDTO;

public interface AuthFacade {
    UserResponseDTO register(RegisterRequestDTO request);

    void verifyRegistration(Long userId, String otp);

    void resendOtp(Long userId);

}
