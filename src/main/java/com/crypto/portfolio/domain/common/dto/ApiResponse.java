package com.crypto.portfolio.domain.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    // Thời gian phản hồi
    @Builder.Default
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp = LocalDateTime.now();

    // Mã code nội bộ
    @Builder.Default
    private int code = 1000;

    // Message thông báo
    private String message;

    // Data trả về
    private T result;

    // Trace ID (Để truy vết lỗi trong Log)
    @Builder.Default
    private String requestId = UUID.randomUUID().toString();
}
