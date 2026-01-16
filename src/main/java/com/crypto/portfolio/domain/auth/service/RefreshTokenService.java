package com.crypto.portfolio.domain.auth.service;

public interface RefreshTokenService {
    Long verifyRefreshToken(String rawToken);

    String createRefreshToken(Long userId, String userAgent, String ipAddress);

    void deleteByRefreshToken(String rawToken);

    void revokeAllUserTokens(Long userId);

    String rotateRefreshToken(String oldRawToken, String ipAddress, String userAgent);

}
