package com.crypto.portfolio.service.serviceImpl;

import com.crypto.portfolio.dto.token.RefreshTokenDTO;
import com.crypto.portfolio.exception.AppException;
import com.crypto.portfolio.exception.ErrorCode;
import com.crypto.portfolio.repository.UserRepository;
import com.crypto.portfolio.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final StringRedisTemplate stringRedisTemplate;
    private final UserRepository userRepository;

    @Value("${app.security.max-devices:2}")
    private int MAX_DEVICES;

    @Value("${app.security.jwt.refresh-token.expiration:604800000}")
    private long refreshTokenDurationMs;

    private static final String RT_KEY_PREFIX = "rt:";
    private static final String USER_RT_LIST_PREFIX = "u_rts:";

    // Verify Refresh Token
    @Override
    public Long verifyRefreshToken(String rawRefreshToken) {
        String refreshTokenKey = RT_KEY_PREFIX + rawRefreshToken;

        Object objRefreshToKen = redisTemplate.opsForValue().get(refreshTokenKey);
        if (objRefreshToKen == null) {
            throw new AppException(ErrorCode.REFRESH_TOKEN_EXPIRED_OR_NOT_EXIST);
        }
        if (objRefreshToKen instanceof RefreshTokenDTO refreshTokenInfo) {
            return refreshTokenInfo.getUserId();
        } else {
            log.error("Dữ liệu Redis sai định dạng tại key: {}", refreshTokenKey);
            throw new AppException(ErrorCode.REFRESH_TOKEN_EXPIRED_OR_NOT_EXIST);
        }
    }

    @Override
    public String createRefreshToken(Long userId, String userAgent, String ipAddress) {
        // Check user tồn tại
        if (!userRepository.existsById(userId)) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }

        String userKey = USER_RT_LIST_PREFIX + userId;

        // Logic Max Devices (Dùng StringRedisTemplate để xử lý List)
        while (true) {
            Long size = stringRedisTemplate.opsForList().size(userKey);

            if (size == null || size < MAX_DEVICES) {
                break;
            }

            String oldRefreshToken = stringRedisTemplate.opsForList().rightPop(userKey);

            if (oldRefreshToken != null) {
                // Xóa key JSON chi tiết (Key này vẫn dùng redisTemplate vì nó chứa Object)
                redisTemplate.delete(RT_KEY_PREFIX + oldRefreshToken);
                log.debug("User {}: Đã xóa thiết bị cũ (Token: {})", userId, oldRefreshToken);
            }
        }

        // Tạo Token mới
        String rawRefreshToken = UUID.randomUUID().toString();
        String refreshTokenKey = RT_KEY_PREFIX + rawRefreshToken;

        // Tạo DTO
        RefreshTokenDTO refreshTokenInfo = RefreshTokenDTO.builder()
                .id(rawRefreshToken)
                .userId(userId)
                .userAgent(userAgent)
                .ipAddress(ipAddress)
                .issuedAt(System.currentTimeMillis())
                .build();

        // Lưu Object chi tiết (Dùng RedisTemplate - JSON)
        redisTemplate.opsForValue().set(refreshTokenKey, refreshTokenInfo, refreshTokenDurationMs, TimeUnit.MILLISECONDS);

        // Lưu ID vào danh sách (SỬA ĐIỂM 2: Dùng StringRedisTemplate cho đồng bộ)
        stringRedisTemplate.opsForList().leftPush(userKey, rawRefreshToken);

        // Set thời gian hết hạn cho danh sách (SỬA ĐIỂM 3: Dùng StringRedisTemplate)
        stringRedisTemplate.expire(userKey, Duration.ofMillis(refreshTokenDurationMs));

        return rawRefreshToken;
    }

    // --- Xóa Refresh Token ---
    @Override
    public void deleteByRefreshToken(String rawToken) {
        String tokenKey = RT_KEY_PREFIX + rawToken;

        // [FIX 4] Lấy DTO để tìm userId
        RefreshTokenDTO tokenInfo = (RefreshTokenDTO) redisTemplate.opsForValue().get(tokenKey);

        if (tokenInfo != null) {
            // Xóa Key 1
            redisTemplate.delete(tokenKey);

            // Xóa Token khỏi danh sách (Key 2)
            String userKey = USER_RT_LIST_PREFIX + tokenInfo.getUserId();
            redisTemplate.opsForList().remove(userKey, 1, rawToken);
        }
    }

    // --- Revoke All ---
    @Override
    public void revokeAllUserTokens(Long userId) {
        String userKey = USER_RT_LIST_PREFIX + userId;

        // range trả về List<Object> do generic type
        List<Object> tokens = redisTemplate.opsForList().range(userKey, 0, -1);

        if (tokens != null && !tokens.isEmpty()) {
            for (Object tokenObj : tokens) {
                String token = tokenObj.toString();
                redisTemplate.delete(RT_KEY_PREFIX + token);
            }
            redisTemplate.delete(userKey);
        }
        log.info("Đã đăng xuất tất cả thiết bị cho User ID: {}", userId);
    }
}