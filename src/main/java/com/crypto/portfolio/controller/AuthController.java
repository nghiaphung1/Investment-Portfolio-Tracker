package com.crypto.portfolio.controller;

import com.crypto.portfolio.dto.config.ApiResponse;
import com.crypto.portfolio.dto.auth.AuthResponseDTO;
import com.crypto.portfolio.dto.auth.LoginRequestDTO;
import com.crypto.portfolio.dto.auth.RegisterRequestDTO;
import com.crypto.portfolio.dto.otp.ResendOtpRequestDTO;
import com.crypto.portfolio.dto.otp.VerifyOtpRequestDTO;
import com.crypto.portfolio.dto.users.UserResponseDTO;
import com.crypto.portfolio.entity.RefreshToken;
import com.crypto.portfolio.entity.User;
import com.crypto.portfolio.exception.AppException;
import com.crypto.portfolio.exception.ErrorCode;
import com.crypto.portfolio.service.AuthService;
import com.crypto.portfolio.service.RefreshTokenService;
import com.crypto.portfolio.service.facade.AuthFacade;
import com.crypto.portfolio.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtUtils jwtUtils;
    private final RefreshTokenService refreshTokenService;
    private final AuthService authService;
    private final AuthFacade authFacade;

    //Login : Tạo Access + Refresh Token, lưu Refresh Token vào Cookie
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> login(@Valid @RequestBody LoginRequestDTO request, HttpServletRequest httpRequest) {

        AuthResponseDTO response = authService.login(request, httpRequest);

        ResponseCookie refreshCookie = jwtUtils.generateRefreshCookie(response.getRefreshToken());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(ApiResponse.<AuthResponseDTO>builder()
                        .message("Đăng nhập thành công")
                        .result(response)
                        .build());
    }

    // Refresh token: Lấy IP/Agent để tạo token mới (Rotation)
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> refreshToken(HttpServletRequest request) {
        String refreshTokenRaw = jwtUtils.getRefreshTokenFromCookies(request);

        if (refreshTokenRaw == null) {
            throw new AppException(ErrorCode.USER_NOT_LOGIN);
        }

        // Verify token cũ
        RefreshToken storedToken = refreshTokenService.verifyRefreshToken(refreshTokenRaw);
        User user = storedToken.getUser();

        // Lấy thông tin thiết bị hiện tại
        String ipAddress = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");

        // Tạo token mới với đầy đủ thông tin thiết bị
        String newRefreshTokenRaw = refreshTokenService.createRefreshToken(user.getId(), userAgent, ipAddress);

        // Tạo Access Token
        String newAccessToken = jwtUtils.generateToken(user.getEmail());

        ResponseCookie newRefreshCookie = jwtUtils.generateRefreshCookie(newRefreshTokenRaw);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, newRefreshCookie.toString())
                .body(ApiResponse.<AuthResponseDTO>builder()
                        .message("Refresh Token thành công")
                        .result(AuthResponseDTO.builder()
                                .accessToken(newAccessToken)
                                .email(user.getEmail())
                                .build())
                        .build());
    }

    // Logout : Xóa Cookie và xóa token trong DB
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {

        // Gọi Service xử lý logic và nhận về Cookie "chết"
        ResponseCookie cleanCookie = authService.logout(request);

        // Trả Response
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cleanCookie.toString())
                .body(ApiResponse.<Void>builder()
                        .message("Đăng xuất thành công")
                        .build());
    }

    // Đăng ký
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponseDTO>> register(@Valid @RequestBody RegisterRequestDTO request) {
        UserResponseDTO result = authFacade.register(request);

        return ResponseEntity.ok(
                ApiResponse.<UserResponseDTO>builder()
                        .code(1000)
                        .message("Đăng ký thành công")
                        .result(result)
                        .build()
        );
    }

    // Xác thực OTP
    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<String>> verifyOtp(@Valid @RequestBody VerifyOtpRequestDTO request) {
        authFacade.verifyRegistration(request.getUserId(), request.getOtp());

        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .message("Xác thực thành công. Tài khoản đã được kích hoạt!")
                        .result("SUCCESS")
                        .build()
        );
    }

    // Gửi lại OTP
    @PostMapping("/resend-otp")
    public ResponseEntity<ApiResponse<Void>> resendOtp(@Valid @RequestBody ResendOtpRequestDTO request) {
        authFacade.resendOtp(request.getUserId());

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .message("Gửi lại mã OTP thành công. Vui lòng kiểm tra email.")
                        .build()
        );
    }


}