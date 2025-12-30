package com.crypto.portfolio.service.facade;

import com.crypto.portfolio.dto.auth.RegisterRequestDTO;
import com.crypto.portfolio.dto.users.UserResponseDTO;

public interface AuthFacade {
    UserResponseDTO register(RegisterRequestDTO request);

    void verifyRegistration(Long userId, String otp);

    void resendOtp(Long userId);

}
