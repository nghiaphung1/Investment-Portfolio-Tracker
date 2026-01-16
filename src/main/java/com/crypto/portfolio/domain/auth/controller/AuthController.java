package com.crypto.portfolio.domain.auth.controller;

import com.crypto.portfolio.domain.common.dto.ApiResponse;
import com.crypto.portfolio.domain.auth.dto.AuthResponseDTO;
import com.crypto.portfolio.domain.auth.dto.LoginRequestDTO;
import com.crypto.portfolio.domain.auth.dto.RegisterRequestDTO;
import com.crypto.portfolio.domain.otp.dto.ResendOtpRequestDTO;
import com.crypto.portfolio.domain.otp.dto.VerifyOtpRequestDTO;
import com.crypto.portfolio.domain.user.dto.UserResponseDTO;
import com.crypto.portfolio.domain.user.repository.UserRepository;
import com.crypto.portfolio.domain.auth.service.AuthService;
import com.crypto.portfolio.domain.auth.service.RefreshTokenService;
import com.crypto.portfolio.domain.auth.facade.AuthFacade;
import com.crypto.portfolio.security.jwt.JwtUtils;
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
    private final UserRepository userRepository;

    //Login : Tạo Access + Refresh Token, lưu Refresh Token vào Cookie
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> login(@Valid @RequestBody LoginRequestDTO request, HttpServletRequest httpRequest) {

        AuthResponseDTO response = authService.login(request, httpRequest);

        ResponseCookie refreshTokenCookie = jwtUtils.generateRefreshTokenCookie(response.getRefreshToken());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(ApiResponse.<AuthResponseDTO>builder()
                        .message("Đăng nhập thành công")
                        .result(response)
                        .build());
    }

    // Refresh token: Lấy IP/Agent để tạo token mới (Rotation)
//    @PostMapping("/refresh")
//    public ResponseEntity<ApiResponse<AuthResponseDTO>> refreshToken(HttpServletRequest request) {
//        // 1. Lấy token từ Cookie
//        String refreshTokenRaw = jwtUtils.getRefreshTokenFromCookies(request);
//
//        if (refreshTokenRaw == null) {
//            throw new AppException(ErrorCode.REFRESH_TOKEN_NOT_EXIST);
//        }
//
//        // 2. Validate Token & Lấy UserId từ Redis (Cực nhanh)
//        Long userId = refreshTokenService.verifyRefreshToken(refreshTokenRaw);
//
//        // 3. Lấy thông tin User mới nhất từ DB
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
//
//        // 4. Token Rotation (Xóa cũ - Tạo mới)
//        // Xóa token cũ để chống tấn công Replay Attack
//        refreshTokenService.deleteByRefreshToken(refreshTokenRaw);
//
//        // Tạo cặp token mới
//        String ipAddress = request.getRemoteAddr();
//        String userAgent = request.getHeader("User-Agent");
//        String newRefreshToken = refreshTokenService.createRefreshToken(user.getId(), userAgent, ipAddress);
//        String newAccessToken = jwtUtils.generateToken(user.getEmail());
//
//        // 5. Trả về
//        ResponseCookie newRefreshCookie = jwtUtils.generateRefreshTokenCookie(newRefreshToken);
//
//        return ResponseEntity.ok()
//                .header(HttpHeaders.SET_COOKIE, newRefreshCookie.toString())
//                .body(ApiResponse.<AuthResponseDTO>builder()
//                        .message("Refresh Token thành công")
//                        .result(AuthResponseDTO.builder()
//                                .accessToken(newAccessToken) // Chỉ trả Access Token mới
//                                .email(user.getEmail())
//                                .build())
//                        .build());
//    }

    // Logout : Xóa Cookie và xóa token trong DB
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        // 1. Lấy token để xóa trong Redis
        String refreshTokenRaw = jwtUtils.getRefreshTokenFromCookies(request);
        if (refreshTokenRaw != null) {
            refreshTokenService.deleteByRefreshToken(refreshTokenRaw);
        }

        // 2. Xóa Cookie ở trình duyệt
        ResponseCookie cleanCookie = jwtUtils.getCleanRefreshTokenCookie();

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