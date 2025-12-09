package com.crypto.portfolio.dto.config;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // Bỏ qua các trường null
public class ApiResponse<T> {

    // 1. Thời gian phản hồi (Format đẹp luôn)
    @Builder.Default
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp = LocalDateTime.now();

    // 2. Mã nghiệp vụ (Business Code: 1000, 9999...)
    @Builder.Default
    private int code = 1000;

    // 3. Thông báo cho người dùng (Human readable)
    private String message;

    // 4. Dữ liệu chính (Payload)
    private T result;

    // 5. Trace ID (Để truy vết lỗi trong Log) - Tự sinh ngẫu nhiên
    @Builder.Default
    private String requestId = UUID.randomUUID().toString();
}
