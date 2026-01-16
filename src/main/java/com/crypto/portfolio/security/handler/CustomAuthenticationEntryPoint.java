package com.crypto.portfolio.security.handler;

import com.crypto.portfolio.domain.common.dto.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final AuthenticationEntryPoint delegate = new BearerTokenAuthenticationEntryPoint();

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        delegate.commence(request, response, authException);

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        // 4. Tạo Body JSON theo chuẩn dự án
        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                .code(401)
                .message("Yêu cầu xác thực. Vui lòng đăng nhập để tiếp tục.")
                .build();

        objectMapper.writeValue(response.getOutputStream(), apiResponse);
    }
}