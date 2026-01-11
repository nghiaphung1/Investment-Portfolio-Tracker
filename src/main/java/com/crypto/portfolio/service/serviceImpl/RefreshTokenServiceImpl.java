package com.crypto.portfolio.service.serviceImpl;

import com.crypto.portfolio.dto.token.RefreshTokenDTO;
import com.crypto.portfolio.exception.AppException;
import com.crypto.portfolio.exception.ErrorCode;
import com.crypto.portfolio.service.RefreshTokenService;
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

    // --- Xác thực Refresh Token ---
    @Override
    public Long verifyRefreshToken(String rawRefreshToken) {
        String refreshTokenKey = RT_KEY_PREFIX + rawRefreshToken;

        Object objRefreshToKen = redisTemplate.opsForValue().get(refreshTokenKey);

        if (objRefreshToKen instanceof RefreshTokenDTO refreshTokenInfo) {
            return refreshTokenInfo.getUserId();
        }
        log.warn("Token verify failed (expired or invalid format): {}", rawRefreshToken);
        throw new AppException(ErrorCode.REFRESH_TOKEN_EXPIRED_OR_NOT_EXIST);
    }

    // --- Tạo Refresh Token ---
    @Override
    public String createRefreshToken(Long userId, String userAgent, String ipAddress) {
        // Tạo user key,raw refresh token, refresh token key
        String userKey = USER_RT_LIST_PREFIX + userId;
        String rawRefreshToken = UUID.randomUUID().toString();
        String refreshTokenKey = RT_KEY_PREFIX + rawRefreshToken;

        // Tạo DTO lưu chi tiết refresh token
        RefreshTokenDTO refreshTokenInfo = RefreshTokenDTO.builder()
                .id(rawRefreshToken)
                .userId(userId)
                .userAgent(userAgent)
                .ipAddress(ipAddress)
                .issuedAt(System.currentTimeMillis())
                .build();

        // Lưu vào Redis
        try {
            // Lưu chi tiết refresh token
            redisTemplate.opsForValue().set(refreshTokenKey, refreshTokenInfo, refreshTokenDurationMs, TimeUnit.MILLISECONDS);

            // Gọi Lua Script lưu token vào danh sách và kiểm soát số lượng thiết bị
            List<String> revokedTokens = stringRedisTemplate.execute(
                    saveRefreshTokenScript,
                    Collections.singletonList(userKey),
                    rawRefreshToken,
                    String.valueOf(MAX_DEVICES),
                    String.valueOf(refreshTokenDurationMs)
            );

            // Dọn dẹp các token bị thu hồi
            if (revokedTokens != null && !revokedTokens.isEmpty()) {
                List<String> keysToDelete = revokedTokens.stream()
                        .map(token -> RT_KEY_PREFIX + token)
                        .collect(Collectors.toList());
                redisTemplate.delete(keysToDelete);
                log.info("User {}: Revoked {} devices.", userId, revokedTokens.size());
            }
        } catch (Exception e) {
            log.error("Redis error when creating refresh token for user {}", userId, e);
            // Xóa key detail vừa tạo để không để lại rác (Rollback thủ công)
            try {
                redisTemplate.delete(refreshTokenKey);
            } catch (Exception ex) {
                log.warn("Failed to rollback (delete) token key during error handling. Key will expire via TTL.");
            }
            throw new AppException(ErrorCode.REDIS_CONNECTION_ERROR);
        }

        return rawRefreshToken;
    }

    // --- Xóa Refresh Token ---
    @Override
    public void deleteByRefreshToken(String rawRefreshToken) {
        String refreshTokenKey = RT_KEY_PREFIX + rawRefreshToken;

        // [FIX 4] Lấy DTO để tìm userId
        Object objRefreshToKen = redisTemplate.opsForValue().get(refreshTokenKey);

        if (objRefreshToKen instanceof RefreshTokenDTO refreshTokenInfo) {
            // Xóa Key 1
            redisTemplate.delete(refreshTokenKey);

            // Xóa Token khỏi danh sách (Key 2)
            String userKey = USER_RT_LIST_PREFIX + refreshTokenInfo.getUserId();
            stringRedisTemplate.opsForList().remove(userKey, 1, rawRefreshToken);
        }
    }

    // --- Revoke All ---
    @Override
    public void revokeAllUserTokens(Long userId) {
        String userKey = USER_RT_LIST_PREFIX + userId;

        // range trả về List<Object> do generic type
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
}