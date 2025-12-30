package com.crypto.portfolio.service.serviceImpl;

import com.crypto.portfolio.dto.auth.AuthResponseDTO;
import com.crypto.portfolio.dto.auth.LoginRequestDTO;
import com.crypto.portfolio.dto.auth.RegisterRequestDTO;
import com.crypto.portfolio.dto.users.UserResponseDTO;
import com.crypto.portfolio.entity.Role;
import com.crypto.portfolio.entity.User;
import com.crypto.portfolio.entity.UserAccount;
import com.crypto.portfolio.type.AuthProvider;
import com.crypto.portfolio.exception.AppException;
import com.crypto.portfolio.exception.ErrorCode;
import com.crypto.portfolio.repository.RoleRepository;
import com.crypto.portfolio.repository.UserAccountRepository;
import com.crypto.portfolio.repository.UserRepository;
import com.crypto.portfolio.security.CustomUserDetails;
import com.crypto.portfolio.service.AuthService;
import com.crypto.portfolio.service.RefreshTokenService;
import com.crypto.portfolio.utils.JwtUtils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    private final RoleRepository roleRepository;

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
    public AuthResponseDTO login(LoginRequestDTO request, HttpServletRequest httpRequest) {
        // Xác thực username/password
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        CustomUserDetails userPrincipal = (CustomUserDetails) authentication.getPrincipal();

        // Lấy thông tin thiết bị từ Request
        String ipAddress = httpRequest.getRemoteAddr();
        String userAgent = httpRequest.getHeader("User-Agent");

        // Tạo Token
        String accessToken = jwtUtils.generateToken(userPrincipal.getUsername());
        String refreshToken = refreshTokenService.createRefreshToken(userPrincipal.getId(), userAgent, ipAddress);

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
        // 1. Lấy token từ cookie
        String refreshTokenRaw = jwtUtils.getRefreshTokenFromCookies(request);

        // 2. Xóa token trong DB (Nếu có)
        if (refreshTokenRaw != null && !refreshTokenRaw.isEmpty()) {
            refreshTokenService.deleteByRefreshToken(refreshTokenRaw);
        }

        // 3. Trả về Cookie đã được làm sạch (MaxAge = 0)
        return jwtUtils.getCleanRefreshTokenCookie();
    }


}