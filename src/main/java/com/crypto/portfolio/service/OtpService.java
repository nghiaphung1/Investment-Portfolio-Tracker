package com.crypto.portfolio.service;

public interface OtpService {
    void saveOtp(Long userId, String rawOtp); // Sửa thành Long

    boolean isSpamming(Long userId); // Sửa thành Long

    void validateOtp(Long userId, String rawOtpInput); // Sửa thành Long

    void resendOtp(Long userId); // Sửa thành Long

}
