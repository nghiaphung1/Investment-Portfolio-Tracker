package com.crypto.portfolio.domain.auth.service;

import com.crypto.portfolio.domain.auth.dto.AuthResponseDTO;
import com.crypto.portfolio.domain.auth.dto.LoginRequestDTO;
import com.crypto.portfolio.domain.auth.dto.RegisterRequestDTO;
import com.crypto.portfolio.domain.user.dto.UserResponseDTO;
import com.crypto.portfolio.domain.iam.entity.Role;
import com.crypto.portfolio.domain.user.entity.User;
import com.crypto.portfolio.domain.user.entity.UserAccount;
import com.crypto.portfolio.constants.AuthProvider;
import com.crypto.portfolio.exception.AppException;
import com.crypto.portfolio.exception.ErrorCode;
import com.crypto.portfolio.domain.iam.repository.RoleRepository;
import com.crypto.portfolio.domain.user.repository.UserAccountRepository;
import com.crypto.portfolio.domain.user.repository.UserRepository;
import com.crypto.portfolio.security.user.CustomUserDetails;
import com.crypto.portfolio.security.jwt.JwtService;
import com.crypto.portfolio.utils.HttpUtils;
import com.crypto.portfolio.security.jwt.JwtUtils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    private final RoleRepository roleRepository;
    private final UserAgentAnalyzer userAgentAnalyzer;
    private final JwtService jwtService;

    // Đăng kí tài khoản Local
    @Override
    @Transactional
    public UserResponseDTO register(RegisterRequestDTO request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_EXISTED);
        }

        User newUser = User.builder()
                .email(request.getEmail())
                .fullName(request.getFullName())
                .enabled(false)
                .build();

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
        newUser.addRole(userRole);
        User savedUser = userRepository.save(newUser);

        UserAccount newAccount = UserAccount.builder()
                .user(savedUser)
                .provider(AuthProvider.LOCAL)
                .password(passwordEncoder.encode(request.getPassword()))
                .build();
        userAccountRepository.save(newAccount);

        return UserResponseDTO.builder()
                .id(savedUser.getId())
                .email(savedUser.getEmail())
                .fullName(savedUser.getFullName())
                .build();
    }

    @Override
    @Transactional
    public void enableUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        user.setEnabled(true);
    }

    // Logic Đăng nhập
    @Override
    @Transactional
    public AuthResponseDTO login(LoginRequestDTO request, HttpServletRequest httpRequest) {
        // Xác thực username/password
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        CustomUserDetails userPrincipal = (CustomUserDetails) authentication.getPrincipal();

        // Lấy IP thật bằng hàm Utility
        String ipAddress = HttpUtils.getClientIp(httpRequest);

        // Lấy User-Agent
        String deviceInfo = extractDeviceInfo(httpRequest);
        // Kết quả: "Chrome 100 on Windows 10 (Desktop)"

        // Tạo Token
        String accessToken = jwtService.createToken(userPrincipal);
        String refreshToken = refreshTokenService.createRefreshToken(userPrincipal.getId(), deviceInfo, ipAddress);

        return AuthResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .id(userPrincipal.getId())
                .email(userPrincipal.getEmail())
                .fullName(userPrincipal.getFullName())
                .avatar(userPrincipal.getAvatar())
                .build();
    }

    //Logic đăng xuất
    @Override
    public ResponseCookie logout(HttpServletRequest request) {
        // Lấy token từ cookie
        String refreshTokenRaw = jwtUtils.getRefreshTokenFromCookies(request);

        // Xóa token trong DB (Nếu có)
        if (refreshTokenRaw != null && !refreshTokenRaw.isEmpty()) {
            refreshTokenService.deleteByRefreshToken(refreshTokenRaw);
        }

        // Trả về Cookie đã được làm sạch (MaxAge = 0)
        return jwtUtils.getCleanRefreshTokenCookie();
    }

    private String extractDeviceInfo(HttpServletRequest httpRequest) {
        String rawUserAgent = httpRequest.getHeader("User-Agent");
        if (rawUserAgent == null) return "Unknown Device";

        try {
            UserAgent agent = userAgentAnalyzer.parse(rawUserAgent);

            String browser = agent.getValue(UserAgent.AGENT_NAME_VERSION_MAJOR);
            String os = agent.getValue(UserAgent.OPERATING_SYSTEM_NAME_VERSION);
            String deviceType = agent.getValue(UserAgent.DEVICE_CLASS);

            return String.format("%s on %s (%s)", browser, os, deviceType);
        } catch (Exception e) {
            // Log warning thôi, không throw exception chặn đăng nhập
            log.warn("Không thể parse User-Agent: {}", rawUserAgent);
            return "Unknown Device (" + rawUserAgent + ")";
        }
    }

}