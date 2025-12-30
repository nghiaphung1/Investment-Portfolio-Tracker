package com.crypto.portfolio.utils.otp;

import lombok.experimental.UtilityClass;

import java.security.SecureRandom;

@UtilityClass
public class OtpGenerator {
    private static final SecureRandom random = new SecureRandom();
    private static final int OTP_BOUND = 1_000_000;
    public String generateOtp() {
        int number = random.nextInt(OTP_BOUND); // Sinh số từ 0 đến 999999
        return String.format("%06d", number); // Luôn đảm bảo đủ 6 chữ số, ví dụ 123 -> 000123
    }
}
