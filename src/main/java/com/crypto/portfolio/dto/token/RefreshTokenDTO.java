package com.crypto.portfolio.dto.token;

import lombok.*;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RefreshTokenDTO implements Serializable {
    // Implement Serializable là thói quen tốt khi lưu Object vào Redis/Session
    private static final long serialVersionUID = 1L;

    private String id;        // Token ID (UUID) - Để frontend biết token nào là token nào khi muốn "Xóa/Logout"
    private Long userId;
    private String userAgent; // Tên thiết bị (Chrome on Windows...)
    private String ipAddress;
    private Long issuedAt;    // Thời gian tạo (Unix Timestamp) - Để hiển thị "Đăng nhập 2 giờ trước"
}
