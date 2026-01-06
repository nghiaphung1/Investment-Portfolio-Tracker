package com.crypto.portfolio.service;

public interface RefreshTokenService {
    Long verifyRefreshToken(String rawToken);

    String createRefreshToken(Long userId, String userAgent, String ipAddress);

    void deleteByRefreshToken(String rawToken);

    void revokeAllUserTokens(Long userId);

}
