package com.crypto.portfolio.controller;

import com.crypto.portfolio.dto.config.ApiResponse;
import com.crypto.portfolio.dto.auth.AuthResponseDTO;
import com.crypto.portfolio.dto.users.LoginRequestDTO;
import com.crypto.portfolio.dto.users.RegisterRequestDTO;
import com.crypto.portfolio.dto.users.UserResponseDTO;
import com.crypto.portfolio.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService; // Chỉ gọi Service, không gọi Repo

    @PostMapping("/register")
    public ApiResponse<UserResponseDTO> register(@Valid @RequestBody RegisterRequestDTO request) {
        UserResponseDTO result = authService.register(request);

        return ApiResponse.<UserResponseDTO>builder()
                .result(result)
                .message("Đăng ký thành công")
                .build();
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        AuthResponseDTO result = authService.login(request);

        return ApiResponse.<AuthResponseDTO>builder()
                .result(result) // Nhớ trả về result, đừng comment nữa nhé :D
                .message("Đăng nhập thành công")
                .build();
    }
}