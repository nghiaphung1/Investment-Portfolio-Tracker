package com.crypto.portfolio.service;

import com.crypto.portfolio.dto.auth.AuthResponseDTO;
import com.crypto.portfolio.dto.auth.LoginRequestDTO;
import com.crypto.portfolio.dto.auth.RegisterRequestDTO;
import com.crypto.portfolio.dto.users.UserResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseCookie;

public interface AuthService {
    UserResponseDTO register(RegisterRequestDTO request);

    void enableUser(Long userId);

    AuthResponseDTO login(LoginRequestDTO request, HttpServletRequest httpRequest);

    ResponseCookie logout(HttpServletRequest request);
}