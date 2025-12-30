package com.crypto.portfolio.service.serviceImpl;

import com.crypto.portfolio.entity.RefreshToken;
import com.crypto.portfolio.entity.User;
import com.crypto.portfolio.exception.AppException;
import com.crypto.portfolio.exception.ErrorCode;
import com.crypto.portfolio.repository.RefreshTokenRepository;
import com.crypto.portfolio.repository.UserRepository;
import com.crypto.portfolio.service.RefreshTokenService;
import com.crypto.portfolio.utils.HashUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RefreshTokenServiceImpl implements RefreshTokenService {

    // Lấy giới hạn thiết bị
    @Value("${app.security.max-devices}")
    private int MAX_DEVICES;

    // Lấy Refresh Token Expiration
    @Value("${app.security.jwt.refresh-token.expiration}")
    private long refreshTokenDurationMs;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    // Verify refresh token từ cookie
    @Override
    public RefreshToken verifyRefreshToken(String rawToken) {
        // Hash refresh token raw từ cookie
        String hashedToken = HashUtils.sha256(rawToken);

        // Tìm refresh token đã hash trong DB
        RefreshToken storedToken = refreshTokenRepository.findByRefreshToken(hashedToken)
                .orElseThrow(() -> new AppException(ErrorCode.REFRESH_TOKEN_NOT_EXIST));

        // Kiểm tra hết hạn của refresh token
        if (storedToken.getExpiryDate().isBefore(Instant.now())) {
            throw new AppException(ErrorCode.REFRESH_TOKEN_EXPIRED);
        }

        return storedToken;
    }

    // Tạo mới refresh token
    @Transactional
    @Override
    public String createRefreshToken(Long userId, String userAgent, String ipAddress) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Lấy danh sách token hiện tại, sắp xếp cũ nhất lên đầu
        List<RefreshToken> existingTokens = refreshTokenRepository.findAllByUserIdOrderByCreatedAtAsc(userId);

        // Nếu đã đạt giới hạn -> Xóa thiết bị cũ nhất
        if (existingTokens.size() >= MAX_DEVICES) {
            int tokensToDeleteCount = existingTokens.size() - MAX_DEVICES + 1;
            List<RefreshToken> tokensToDelete = existingTokens.subList(0, tokensToDeleteCount);
            //Xóa 1 lần nhiều token
            refreshTokenRepository.deleteAll(tokensToDelete);
            log.debug("Đã xóa {} thiết bị cũ nhất của user {}", tokensToDeleteCount, userId);
        }

        // Tạo chuỗi ngẫu nhiên (32 bytes)
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        // Hash bằng SHA-256 trước khi lưu
        String hashedToken = HashUtils.sha256(rawToken);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .refreshToken(hashedToken)
                .expiryDate(Instant.now().plusMillis(refreshTokenDurationMs))
                .userAgent(userAgent)
                .ipAddress(ipAddress)
                .build();

        refreshTokenRepository.save(refreshToken);

        // Trả về raw refresh token cho user cất vào Cookie
        return rawToken;
    }

    /**
     * Chức năng Đăng xuất: Xóa token cụ thể
     */
    @Transactional
    @Override
    public void deleteByRefreshToken(String rawToken) {
        String hashedToken = HashUtils.sha256(rawToken);
        refreshTokenRepository.findByRefreshToken(hashedToken).ifPresent(refreshTokenRepository::delete);
    }

    /**
     * Chức năng Bảo mật: Đăng xuất khỏi TẤT CẢ thiết bị (Dùng khi đổi mật khẩu)
     */

    @Transactional
    @Override
    public void revokeAllUserTokens(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
//        log.info("Revoked all sessions for User ID: {}", userId);
    }

    /**
     * Cron Job: Tự động chạy lúc 2:00 sáng mỗi ngày để dọn rác
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    @Override
    public void cleanupExpiredTokens() {
        Instant now = Instant.now();
        log.info("Bắt đầu dọn dẹp Refresh Token hết hạn lúc: {}", now);
        refreshTokenRepository.deleteByExpiryDateBefore(now);
        log.info("Đã dọn dẹp xong!");
    }
}