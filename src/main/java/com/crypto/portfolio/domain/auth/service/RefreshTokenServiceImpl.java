package com.crypto.portfolio.domain.auth.service;

import com.crypto.portfolio.domain.auth.dto.RefreshTokenDTO;
import com.crypto.portfolio.exception.AppException;
import com.crypto.portfolio.exception.ErrorCode;
import com.crypto.portfolio.utils.HashUtils; // Import HashUtils
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final StringRedisTemplate stringRedisTemplate;
    private final DefaultRedisScript<List> saveRefreshTokenScript;

    @Value("${app.security.max-devices:2}")
    private int MAX_DEVICES;

    @Value("${app.security.jwt.refresh-token.expiration:604800000}")
    private long refreshTokenDurationMs;

    private static final String RT_KEY_PREFIX = "rt:";
    private static final String USER_RT_LIST_PREFIX = "u_rts:";

    // Xác thực Refresh Token
    @Override
    public Long verifyRefreshToken(String rawRefreshToken) {
        // Hash token user gửi lên để tìm trong Redis
        String hashedToken = HashUtils.sha256(rawRefreshToken);
        String refreshTokenKey = RT_KEY_PREFIX + hashedToken;

        Object objRefreshToKen = redisTemplate.opsForValue().get(refreshTokenKey);

        if (objRefreshToKen instanceof RefreshTokenDTO refreshTokenInfo) {
            return refreshTokenInfo.getUserId();
        }
        log.warn("Token verify failed (expired or invalid format). Hash: {}", hashedToken);
        throw new AppException(ErrorCode.REFRESH_TOKEN_EXPIRED_OR_NOT_EXIST);
    }

    // Tạo Refresh Token
    @Override
    public String createRefreshToken(Long userId, String userAgent, String ipAddress) {
        // Tạo Raw Refresh Token
        String rawRefreshToken = UUID.randomUUID().toString();

        // Hash Token
        String hashedToken = HashUtils.sha256(rawRefreshToken);

        String userKey = USER_RT_LIST_PREFIX + userId;
        String refreshTokenKey = RT_KEY_PREFIX + hashedToken;

        // Tạo DTO lưu chi tiết refresh token (Lưu ID là hash để đồng bộ)
        RefreshTokenDTO refreshTokenInfo = RefreshTokenDTO.builder()
                .id(hashedToken)
                .userId(userId)
                .userAgent(userAgent)
                .ipAddress(ipAddress)
                .issuedAt(System.currentTimeMillis())
                .build();

        // Lưu vào Redis
        try {
            // Lưu chi tiết (Key = Hash)
            redisTemplate.opsForValue().set(refreshTokenKey, refreshTokenInfo, refreshTokenDurationMs, TimeUnit.MILLISECONDS);

            // Gọi Lua Script (Lưu Hash vào list thiết bị)
            List<String> revokedTokens = stringRedisTemplate.execute(
                    saveRefreshTokenScript,
                    Collections.singletonList(userKey),
                    hashedToken, // Truyền Hash vào script
                    String.valueOf(MAX_DEVICES),
                    String.valueOf(refreshTokenDurationMs)
            );

            // Dọn dẹp các token bị thu hồi (Lúc này revokedTokens chứa các Hash)
            if (revokedTokens != null && !revokedTokens.isEmpty()) {
                List<String> keysToDelete = revokedTokens.stream()
                        .map(token -> RT_KEY_PREFIX + token) // token ở đây là hash rồi, đúng logic
                        .collect(Collectors.toList());
                redisTemplate.delete(keysToDelete);
                log.info("User {}: Revoked {} devices.", userId, revokedTokens.size());
            }
        } catch (Exception e) {
            log.error("Redis error when creating refresh token for user {}", userId, e);
            try {
                redisTemplate.delete(refreshTokenKey);
            } catch (Exception ex) {
                log.warn("Rollback failed for key {}", refreshTokenKey);
            }
            throw new AppException(ErrorCode.REDIS_CONNECTION_ERROR);
        }

        return rawRefreshToken;
    }

    // Xóa Refresh Token (Logout)
    @Override
    public void deleteByRefreshToken(String rawRefreshToken) {
        // Băm để tìm key xóa
        String hashedToken = HashUtils.sha256(rawRefreshToken);
        String refreshTokenKey = RT_KEY_PREFIX + hashedToken;

        Object objRefreshToKen = redisTemplate.opsForValue().get(refreshTokenKey);

        if (objRefreshToKen instanceof RefreshTokenDTO refreshTokenInfo) {
            redisTemplate.delete(refreshTokenKey);

            String userKey = USER_RT_LIST_PREFIX + refreshTokenInfo.getUserId();
            // Xóa Hash khỏi list
            stringRedisTemplate.opsForList().remove(userKey, 1, hashedToken);
        }
    }

    // Revoke All
    @Override
    public void revokeAllUserTokens(Long userId) {
        String userKey = USER_RT_LIST_PREFIX + userId;
        List<String> tokens = stringRedisTemplate.opsForList().range(userKey, 0, -1);

        if (tokens != null && !tokens.isEmpty()) {
            List<String> keysToDelete = tokens.stream()
                    .map(token -> RT_KEY_PREFIX + token)
                    .collect(Collectors.toList());

            redisTemplate.delete(keysToDelete);
            stringRedisTemplate.delete(userKey);
        }
        log.info("Đã đăng xuất tất cả thiết bị cho User ID: {}", userId);
    }

    // Cấp lại Refresh Token mới
    @Override
    public String rotateRefreshToken(String oldRawToken, String ipAddress, String userAgent) {
        // Xác thực token cũ (Check xem có tồn tại trong Redis không)
        Long userId = verifyRefreshToken(oldRawToken);

        // Xóa Token cũ (Revoke)
        deleteByRefreshToken(oldRawToken);

        // Tạo Token mới
        return createRefreshToken(userId, userAgent, ipAddress);
    }
}