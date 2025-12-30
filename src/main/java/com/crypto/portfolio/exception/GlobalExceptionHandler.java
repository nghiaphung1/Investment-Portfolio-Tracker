package com.crypto.portfolio.exception;

import com.crypto.portfolio.dto.config.ApiResponse;
import lombok.extern.slf4j.Slf4j; // 1. Thêm Lombok Log
import org.springframework.dao.QueryTimeoutException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // Xử lý Validation
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<String>> handleValidationException(MethodArgumentNotValidException ex) {
        String enumKey = ex.getFieldError() != null ? ex.getFieldError().getDefaultMessage() : "INVALID_KEY";

        ErrorCode errorCode;
        try {
            errorCode = ErrorCode.valueOf(enumKey);
        } catch (IllegalArgumentException e) {
            errorCode = ErrorCode.INVALID_KEY;
        }

        return ResponseEntity
                .badRequest()
                .body(ApiResponse.<String>builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build());
    }

    // Xử lý sai định dạng JSON
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<String>> handleInvalidFormatException(HttpMessageNotReadableException ex) {
        log.warn("JSON Format Error: ", ex); // Log warning

        ErrorCode errorCode = ErrorCode.INVALID_JSON_FORMAT;
        String detailMessage = errorCode.getMessage();

        if (ex.getCause() instanceof com.fasterxml.jackson.databind.exc.InvalidFormatException iex) {
            if (!iex.getPath().isEmpty()) {
                String fieldName = iex.getPath().get(0).getFieldName();
                detailMessage += ": Sai định dạng tại trường '" + fieldName + "'";
            }
        }

        return ResponseEntity
                .badRequest()
                .body(ApiResponse.<String>builder()
                        .code(errorCode.getCode())
                        .message(detailMessage)
                        .build());
    }

    // 3. Xử lý logic nghiệp vụ (Quan trọng: Map Status code chuẩn)
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<String>> handleAppException(AppException ex) {
        ErrorCode errorCode = ex.getErrorCode();

        // Log lỗi nghiệp vụ (tùy mức độ nghiêm trọng)
        log.error("App Exception: Code={}, Message={}", errorCode.getCode(), errorCode.getMessage());

        return ResponseEntity
                .status(errorCode.getStatusCode()) // 3. ErrorCode nên có method getStatusCode() trả về HttpStatus
                .body(ApiResponse.<String>builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build());
    }

    // 4. Xử lý upload file quá lớn
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<String>> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        ErrorCode errorCode = ErrorCode.FILE_TOO_LARGE;

        return ResponseEntity
                .status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(ApiResponse.<String>builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build());
    }

    // Xử lý lỗi kết nối Redis/DB
    @ExceptionHandler({RedisConnectionFailureException.class, QueryTimeoutException.class})
    public ResponseEntity<ApiResponse<String>> handleServiceUnavailable(Exception ex) {
        log.error("Infrastructure Error (Redis/DB): ", ex);
        ErrorCode errorCode = ErrorCode.SERVICE_UNAVAILABLE;

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.<String>builder()
                        .code(errorCode.getCode())
                        .message("Dịch vụ tạm thời gián đoạn. Vui lòng thử lại sau ít phút.")
                        .build());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<String>> handleAuthenticationException(AuthenticationException ex) {
        ErrorCode errorCode = ErrorCode.INVALID_CREDENTIALS; // Mặc định là sai thông tin

        if (ex instanceof DisabledException) {
            errorCode = ErrorCode.USER_NOT_ENABLED;
        }

        log.warn("Authentication failed: {}", ex.getMessage());

        return ResponseEntity
                .status(errorCode.getStatusCode())
                .body(ApiResponse.<String>builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build());
    }

    // 5. Xử lý lỗi hệ thống (500)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleUnwantedException(Exception ex) {
        // 2. Log full stack trace để debug (nhưng không show cho user)
        log.error("Uncaught Exception: ", ex);

        ErrorCode errorCode = ErrorCode.UNCATEGORIZED_EXCEPTION;

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.<String>builder()
                        .code(errorCode.getCode())
                        .message("Lỗi hệ thống nội bộ. Vui lòng liên hệ Admin.") // Giấu message lỗi gốc đi
                        .build());
    }
}