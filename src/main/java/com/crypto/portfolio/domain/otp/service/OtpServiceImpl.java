package com.crypto.portfolio.domain.otp.service;

import com.crypto.portfolio.exception.AppException;
import com.crypto.portfolio.exception.ErrorCode;
import com.crypto.portfolio.domain.otp.core.OtpHasher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpServiceImpl implements OtpService {
    private final StringRedisTemplate redisTemplate;
    private final DefaultRedisScript<String> saveOtpScript;
    private final DefaultRedisScript <String> validateOtpScript;
    private final DefaultRedisScript <String> resendOtpScript;

    // Key design: prefix:type:userId
    private static final String OTP_KEY = "otp:val:%s";
    private static final String LOCK_KEY = "otp:lock:%s";
    private static final String FAIL_KEY = "otp:fail:%s";

    @Value("${app.otp.secret}")
    private String otpSecret;

    @Override
    public void saveOtp(Long userId, String rawOtp) {
        String otpKey = String.format(OTP_KEY, userId);
        String lockKey = String.format(LOCK_KEY, userId);

        // Hash OTP với HMAC-SHA256
        String hashedOtp = OtpHasher.hmacSha256(rawOtp, String.valueOf(userId), otpSecret);

        try {
            // Thực thi Script
            redisTemplate.execute(
                    saveOtpScript,
                    Arrays.asList(otpKey, lockKey), // KEYS
                    hashedOtp, "300",                // ARGV[1, 2]
                    "LOCKED", "60"                  // ARGV[3, 4]
            );
            log.info("Successfully saved OTP and Lock atomically for user: {}", userId);
        } catch (Exception e) {
            log.error("Redis Lua execution error: ", e);
            throw new AppException(ErrorCode.SERVICE_UNAVAILABLE);
        }
    }

    @Override
    public boolean isSpamming(Long userId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(String.format(LOCK_KEY, userId)));
    }

    @Override
    public void validateOtp(Long userId, String rawOtp) {
        String otpKey = String.format(OTP_KEY, userId);
        String failKey = String.format(FAIL_KEY, userId);
        String hashedOtp = OtpHasher.hmacSha256(rawOtp, String.valueOf(userId), otpSecret);

        // Thực thi Lua Script
        String result = redisTemplate.execute(
                validateOtpScript,
                Arrays.asList(otpKey, failKey), // KEYS[1], KEYS[2]
                hashedOtp,                     // ARGV[1]
                "5",                           // ARGV[2]: Tối đa 5 lần sai
                "600"                          // ARGV[3]: Khóa 10 phút (600s) nếu sai
        );

        // Xử lý kết quả trả về từ Lua
        switch (result) {
            case "SUCCESS":
                log.info("OTP verified successfully for user: {}", userId);
                break;
            case "BLOCKED":
                throw new AppException(ErrorCode.TOO_MANY_ATTEMPTS);
            case "EXPIRED":
                throw new AppException(ErrorCode.OTP_EXPIRED);
            case "INVALID":
                throw new AppException(ErrorCode.INVALID_OTP);
            default:
                throw new AppException(ErrorCode.ERROR_NOT_FOUND);
        }
    }

    @Override
    public void resendOtp(Long userId) {
        String lockKey = String.format(LOCK_KEY, userId);
        String failKey = String.format(FAIL_KEY, userId);

        // 1. Chạy Lua script để kiểm tra spam
        // Truyền vào 60 giây (hoặc lấy từ config) để chặn người dùng click liên tục
        String result = redisTemplate.execute(
                resendOtpScript,
                Arrays.asList(lockKey),
                "60"
        );

        if ("TOO_FAST".equals(result)) {
            throw new AppException(ErrorCode.OTP_SEND_TOO_FAST);
        }

        // 2. Nếu PROCEED, chúng ta nên reset luôn số lần thử sai (failKey)
        // để người dùng có 5 lượt thử mới với mã mới.
        redisTemplate.delete(failKey);

        // Lưu ý: Sau khi gọi resendOtp trong AuthFacade,
        // bạn phải gọi tiếp hàm saveOtp() để tạo mã mới và gửi mail.
        log.info("Resend OTP allowed for user: {}. Resetting fail count.", userId);
    }
}
