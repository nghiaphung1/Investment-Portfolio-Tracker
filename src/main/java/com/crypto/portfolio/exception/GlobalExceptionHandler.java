package com.crypto.portfolio.exception;

import com.crypto.portfolio.dto.config.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice // Đây là nơi "nghe ngóng" mọi lỗi trong hệ thống
public class GlobalExceptionHandler {

    // 1. Bắt lỗi Validation (@NotNull, @Min...)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<String>> handleValidationException(MethodArgumentNotValidException ex) {
        // Giả sử bạn validate ở DTO: @NotBlank(message = "INVALID_SYMBOL")
        String enumKey = ex.getFieldError().getDefaultMessage();

        ErrorCode errorCode;
        try {
            errorCode = ErrorCode.valueOf(enumKey);
        } catch (IllegalArgumentException e) {
            errorCode = ErrorCode.INVALID_KEY;
        }

        ApiResponse<String> response = ApiResponse.<String>builder()
                .code(errorCode.getCode()) // Lấy số 2001
                .message(errorCode.getMessage()) // Lấy chữ "Tên coin không được để trống"
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    //2. Bắt lỗi khi người dùng nhập sai kiểu dữ liệu
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<String>> handleInvalidFormatException(HttpMessageNotReadableException ex) {
        ErrorCode errorCode = ErrorCode.INVALID_JSON_FORMAT;
        String detailMessage = errorCode.getMessage();

        // Mẹo: Cố gắng lấy tên trường bị sai từ exception của Jackson
        if (ex.getCause() instanceof com.fasterxml.jackson.databind.exc.InvalidFormatException) {
            com.fasterxml.jackson.databind.exc.InvalidFormatException iex =
                    (com.fasterxml.jackson.databind.exc.InvalidFormatException) ex.getCause();

            // Lấy tên trường bị lỗi (ví dụ: type, quantity)
            if (!iex.getPath().isEmpty()) {
                String fieldName = iex.getPath().get(0).getFieldName();
                detailMessage += ": Sai tại trường '" + fieldName + "'";
            }
        }

        ApiResponse<String> response = ApiResponse.<String>builder()
                .code(errorCode.getCode())
                .message(detailMessage) // Kết quả: "Dữ liệu... : Sai tại trường 'type'"
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<String>> handleAppException(AppException ex) {
        ErrorCode errorCode = ex.getErrorCode();

        ApiResponse<String> response = ApiResponse.<String>builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    //2. Bắt tất cả các lỗi lạ khác (NullPointer, Database Error...)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleUnwantedException(Exception ex) {
        ErrorCode errorCode = ErrorCode.ERROR_NOT_FOUND;
        ApiResponse<String> response = ApiResponse.<String>builder()
                .code(errorCode.getCode()) // Mã lỗi hệ thống
                .message(errorCode.getMessage()) // Dev xem tạm, Product thật thì nên giấu đi
                .build();

        return ResponseEntity.internalServerError().body(response);
    }



}
