package com.crypto.portfolio.security.handler;

import com.crypto.portfolio.dto.config.ApiResponse; // Import DTO chuẩn
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    // 1. Sử dụng Delegate chuẩn của Spring Security (dành cho OAuth2 Resource Server)
    // Class này sẽ tự động set Status 403 và Header "WWW-Authenticate: Bearer error=insufficient_scope"
    private final AccessDeniedHandler delegate = new BearerTokenAccessDeniedHandler();

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {

        delegate.handle(request, response, accessDeniedException);

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                .code(403)
                .message("Bạn không có quyền truy cập tài nguyên này")
                .build();

        objectMapper.writeValue(response.getOutputStream(), apiResponse);
    }
}