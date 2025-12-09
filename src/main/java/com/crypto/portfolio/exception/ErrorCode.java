package com.crypto.portfolio.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    //1. COMMON ERROR (Lỗi chung hệ thống & Input) - Range: 1000 -> 1999
    SUCCESS(1000, "Thành công"),
    UNCATEGORIZED_EXCEPTION(9999, "Lỗi hệ thống chưa xác định"),
    INVALID_KEY(1001, "Sai key message rồi"), // Lỗi mapping message
    INVALID_JSON_FORMAT(1002, "Dữ liệu đầu vào sai định dạng (Kiểm tra lại kiểu dữ liệu, Enum, Date)"),

    ERROR_NOT_FOUND(99999, "Lỗi hệ thống chưa phát hiện"),

    //3. MODULE USER - Range: 3000 -> 3999
    USER_EXISTED(3001, "Tên đăng nhập đã tồn tại"),

    //2. MODULE TRANSACTION - Range: 2000 -> 2999
    INVALID_SYMBOL(2001, "Tên coin không được để trống"),
    INVALID_QUANTITY(2002, "Số lượng phải lớn hơn 0"),
    QUANTITY_MUST_BE_POSITIVE(2003, "Số lượng phải lớn hơn 0"),
    INVALID_PRICE_PER_COIN(2004, "Giá không được để trống"),
    PRICE_CANNOT_BE_NEGATIVE(2005, "Giá không được âm"),
    INVALID_TRANSACTION_TYPE(2006, "Kiểu giao dịch phải là BUY hoặc SELL");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
