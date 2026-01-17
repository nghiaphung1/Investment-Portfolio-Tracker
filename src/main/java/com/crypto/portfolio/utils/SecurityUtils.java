package com.crypto.portfolio.utils;

import com.crypto.portfolio.exception.AppException;
import com.crypto.portfolio.exception.ErrorCode;
import lombok.experimental.UtilityClass;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

@UtilityClass
public class SecurityUtils {

    public Long getCurrentUserId() {
        String sub = getJwt().getSubject();
        try {
            return Long.valueOf(sub);
        } catch (NumberFormatException e) {
            throw new AppException(ErrorCode.PARSE_ERROR);
        }
    }

    public String getCurrentUserEmail() {
        Jwt jwt = getJwt();
        if (jwt.hasClaim("email")) {
            return jwt.getClaimAsString("email");
        }
        return null;
    }

    public Long getCurrentUserIdOrNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            String sub = jwtAuth.getToken().getSubject();
            try {
                return Long.valueOf(sub);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private Jwt getJwt() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            return jwtAuth.getToken();
        }

        throw new AppException(ErrorCode.USER_NOT_LOGIN);
    }
}