package com.crypto.portfolio.domain.auth.service;

import com.crypto.portfolio.domain.auth.dto.AuthResponseDTO;
import com.crypto.portfolio.domain.auth.dto.LoginRequestDTO;
import com.crypto.portfolio.domain.auth.dto.RegisterRequestDTO;
import com.crypto.portfolio.domain.user.dto.UserResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseCookie;

public interface AuthService {
    UserResponseDTO register(RegisterRequestDTO request);

    void enableUser(Long userId);

    AuthResponseDTO login(LoginRequestDTO request, HttpServletRequest httpRequest);

    ResponseCookie logout(HttpServletRequest request);
}