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

    @Value("${app.security.jwt.secret}")
    private String secretKey;

    @Value("${app.security.jwt.access-token.expiration}")
    private long jwtExpiration;

    @Value("${app.security.jwt.refresh-token.expiration}")
    private long refreshExpiration;

    @Value("${app.security.jwt.refresh-token.cookie-max-age}")
    private long refreshTokenCookieMaxAge;

    // Tên Cookie
    private final String REFRESH_COOKIE_NAME = "refreshToken";

    // 1. Lấy Username từ Token
    public String extractUsername(String token) {
        try {
            return extractClaim(token, Claims::getSubject);
        } catch (ExpiredJwtException e) {
            return e.getClaims().getSubject();
        } catch (Exception e) {
            return null;
        }
    }

    // 2. Lấy một thông tin cụ thể
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // 3. Tạo Token
    public String generateToken(String username) {
        return buildToken(new HashMap<>(), username);
    }

    // 4. Logic tạo Token chi tiết
    private String buildToken(Map<String, Object> extraClaims, String username) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // 5. Kiểm tra Token
    public boolean isTokenValid(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username)) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Tạo Cookie chứa Refresh Token
    public ResponseCookie generateRefreshCookie(String token) {
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