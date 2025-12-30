package com.crypto.portfolio.service;

import com.crypto.portfolio.entity.RefreshToken;

public interface RefreshTokenService {
    RefreshToken verifyRefreshToken(String rawToken);

    String createRefreshToken(Long userId, String userAgent, String ipAddress);

    void deleteByRefreshToken(String rawToken);

    void revokeAllUserTokens(Long userId);

    void cleanupExpiredTokens();
}
