package com.crypto.portfolio.dto.auth;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthResponseDTO {
    private String token;
    private String username;
    // Có thể thêm role, avatarUrl... nếu muốn
}
