package com.crypto.portfolio.exception;

import lombok.Getter;

@Getter
public class AppException extends RuntimeException {
    private ErrorCode errorCode;

    public AppException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
    public AppException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause); // Truyền cause cho cha (RuntimeException) giữ hộ
        this.errorCode = errorCode;
    }
}
