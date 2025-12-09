package com.crypto.portfolio.service;

import com.crypto.portfolio.dto.auth.AuthResponseDTO;
import com.crypto.portfolio.dto.users.LoginRequestDTO;
import com.crypto.portfolio.dto.users.RegisterRequestDTO;
import com.crypto.portfolio.dto.users.UserResponseDTO;
import com.crypto.portfolio.entity.User;
import com.crypto.portfolio.entity.UserAccount;
import com.crypto.portfolio.entity.type.AuthProvider;
import com.crypto.portfolio.exception.AppException;
import com.crypto.portfolio.exception.ErrorCode;
import com.crypto.portfolio.repository.UserAccountRepository;
import com.crypto.portfolio.repository.UserRepository;
import com.crypto.portfolio.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserAccountRepository userAccountRepository; // Cần thêm cái này
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;

    // Logic Đăng ký (Local)
    public UserResponseDTO register(RegisterRequestDTO request) {
        // 1. Check trùng Email (Kiến trúc mới dùng Email làm định danh)
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        // 2. Tạo User Identity (Không có password)
        User user = User.builder()
                .email(request.getEmail())
                .fullName(request.getFullName())
                .build();
        User savedUser = userRepository.save(user);

        // 3. Tạo UserAccount để lưu Password (QUAN TRỌNG)
        UserAccount account = UserAccount.builder()
                .user(savedUser)
                .provider(AuthProvider.LOCAL) // Đánh dấu là tài khoản Local
                .password(passwordEncoder.encode(request.getPassword())) // Mã hóa pass lưu vào đây
                .build();
        userAccountRepository.save(account);

        // 4. Trả về DTO
        return UserResponseDTO.builder()
                .id(savedUser.getId())
                .username(savedUser.getEmail())
                .fullName(savedUser.getFullName())
                .email(savedUser.getEmail())
                .build();
    }

    // Logic Đăng nhập
    public AuthResponseDTO login(LoginRequestDTO request) {
        // AuthenticationManager sẽ tự gọi CustomUserDetailsService để check pass
        // (Bạn đã sửa CustomUserDetailsService để lấy pass từ bảng UserAccount rồi nên chỗ này OK)
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        // Tạo Token từ Username (Email)
        String token = jwtUtils.generateToken(request.getUsername());

        return AuthResponseDTO.builder()
                .token(token)
                .username(request.getUsername())
                .build();
    }
}