package com.crypto.portfolio.domain.auth.facade;

import com.crypto.portfolio.annotation.Facade;
import com.crypto.portfolio.domain.auth.dto.RegisterRequestDTO;
import com.crypto.portfolio.domain.user.dto.UserResponseDTO;
import com.crypto.portfolio.domain.user.entity.User;
import com.crypto.portfolio.event.OnNotificationEvent;
import com.crypto.portfolio.exception.AppException;
import com.crypto.portfolio.exception.ErrorCode;
import com.crypto.portfolio.domain.user.repository.UserRepository;
import com.crypto.portfolio.domain.auth.service.AuthService;
import com.crypto.portfolio.domain.otp.service.OtpService;
import com.crypto.portfolio.domain.otp.core.OtpGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Facade
@RequiredArgsConstructor
public class AuthFacadeImpl implements AuthFacade {
    private final AuthService authService;
    private final OtpService otpService;
    private final ApplicationEventPublisher eventPublisher;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserResponseDTO register(RegisterRequestDTO request) {
        UserResponseDTO savedUser = authService.register(request);

        if (otpService.isSpamming(savedUser.getId())) {
            throw new AppException(ErrorCode.OTP_SEND_TOO_FAST);
        }

        String rawOtpCode = OtpGenerator.generateOtp();
        otpService.saveOtp(savedUser.getId(), rawOtpCode);

        Map<String, Object> data = new HashMap<>();
        data.put("fullName", savedUser.getFullName());
        data.put("otpCode", rawOtpCode);

        eventPublisher.publishEvent(new OnNotificationEvent(
                savedUser.getEmail(),
                savedUser.getFullName(),
                "Xác thực đăng ký tài khoản",
                "otp-registration",
                data
        ));

        return savedUser;
    }

    @Override
    @Transactional
    public void verifyRegistration(Long userId, String otp) {
        otpService.validateOtp(userId, otp);
        authService.enableUser(userId);
    }

    @Override
    public void resendOtp(Long userId) {
        // 1. Check spam + Reset count lỗi (Lua)
        otpService.resendOtp(userId);

        // 2. Tạo mã ngẫu nhiên mới
        String newOtp = OtpGenerator.generateOtp();

        // 3. Lưu OTP mới vào Redis (Sẽ ghi đè mã cũ)
        otpService.saveOtp(userId, newOtp);

        // 4. Bắn Event gửi Email
        User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        eventPublisher.publishEvent(new OnNotificationEvent(
                user.getEmail(),
                user.getFullName(),
                "Gửi lại mã xác thực",
                "otp-registration",
                Map.of("fullName", user.getFullName(), "otpCode", newOtp)
        ));
    }
}
