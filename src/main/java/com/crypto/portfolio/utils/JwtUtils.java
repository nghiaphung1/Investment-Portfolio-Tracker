package com.crypto.portfolio.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtils {

    @Value("${app.security.jwt.refresh-token.cookie-max-age}")
    private long refreshTokenCookieMaxAge;
    // Tên Cookie
    private final String REFRESH_COOKIE_NAME = "refreshToken";

    // Tạo Cookie chứa Refresh Token
    public ResponseCookie generateRefreshTokenCookie(String token) {
        return ResponseCookie.from(REFRESH_COOKIE_NAME, token)
                .path("/")
                .maxAge(refreshTokenCookieMaxAge) // Dùng biến config (7 ngày)
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .build();
    }

    // Hàm xóa sạch Cookie (Logout)
    public ResponseCookie getCleanRefreshTokenCookie() {
        return ResponseCookie.from(REFRESH_COOKIE_NAME, "")
                .path("/")
                .httpOnly(true)  // Khớp với lúc tạo
                .secure(false)   // Khớp với lúc tạo (Lên PROD nhớ đổi thành true)
                .sameSite("Lax") // Khớp với lúc tạo
                .maxAge(0)       // Thời gian sống = 0 -> Xóa ngay
                .build();
    }


    // Lấy Refresh Token từ Cookie
    public String getRefreshTokenFromCookies(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, REFRESH_COOKIE_NAME);
        if (cookie != null) {
            return cookie.getValue();
        }
        return null;
    }
}