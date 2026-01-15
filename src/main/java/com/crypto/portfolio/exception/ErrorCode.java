package com.crypto.portfolio.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // 1. COMMON ERROR (Lỗi chung hệ thống & Input) - Range: 1000 -> 1999
    SUCCESS(1000, "Thành công", HttpStatus.OK),
    UNCATEGORIZED_EXCEPTION(9999, "Lỗi hệ thống chưa xác định", HttpStatus.INTERNAL_SERVER_ERROR), // 500
    INVALID_KEY(1001, "Sai key message rồi", HttpStatus.INTERNAL_SERVER_ERROR), // 500 (Lỗi dev)
    INVALID_JSON_FORMAT(1002, "Dữ liệu đầu vào sai định dạng", HttpStatus.BAD_REQUEST), // 400
    ERROR_NOT_FOUND(99999, "Lỗi hệ thống chưa phát hiện", HttpStatus.INTERNAL_SERVER_ERROR), // 500
    THREAD_INTERRUPTED(1003, "Luồng bị gián đoạn", HttpStatus.INTERNAL_SERVER_ERROR), // 500
    SERVICE_UNAVAILABLE(1004, "Dịch vụ tạm thời gián đoạn. Vui lòng thử lại sau ít phút.", HttpStatus.SERVICE_UNAVAILABLE), // 503
    REDIS_CONNECTION_ERROR(1005, "Kết nối Redis thất bại", HttpStatus.INTERNAL_SERVER_ERROR), // 500
    PARSE_ERROR(1006, "Lỗi khi chuyển đổi dữ liệu", HttpStatus.INTERNAL_SERVER_ERROR), // 500

    // 2. MODULE TRANSACTION - Range: 2000 -> 2999
    INVALID_SYMBOL(2001, "Tên coin không được để trống", HttpStatus.BAD_REQUEST),
    INVALID_QUANTITY(2002, "Số lượng phải lớn hơn 0", HttpStatus.BAD_REQUEST),
    QUANTITY_MUST_BE_POSITIVE(2003, "Số lượng phải lớn hơn 0", HttpStatus.BAD_REQUEST),
    INVALID_PRICE_PER_COIN(2004, "Giá không được để trống", HttpStatus.BAD_REQUEST),
    PRICE_CANNOT_BE_NEGATIVE(2005, "Giá không được âm", HttpStatus.BAD_REQUEST),
    INVALID_TRANSACTION_TYPE(2006, "Kiểu giao dịch phải là BUY hoặc SELL", HttpStatus.BAD_REQUEST),

    // 3. MODULE USER - Range: 3000 -> 3999
    USER_NOT_FOUND(3001, "Người dùng không tồn tại", HttpStatus.NOT_FOUND), // 404
    USER_ID_SHOULD_NOT_BE_NULL(3002, "ID người dùng không được để trống", HttpStatus.BAD_REQUEST),

    // 4. MODULE AUTH - Range: 4000 -> 4999
    USER_NOT_LOGIN(4001, "Người dùng chưa đăng nhập", HttpStatus.UNAUTHORIZED), // 401
    INVALID_CREDENTIALS(4002, "Email hoặc mật khẩu không đúng", HttpStatus.UNAUTHORIZED), // 401
    PASSWORD_TOO_LONG(4003, "Mật khẩu không được vượt quá 72 ký tự", HttpStatus.BAD_REQUEST),
    EMAIL_EXISTED(4004, "Email đã tồn tại", HttpStatus.CONFLICT),
    INVALID_EMAIL_FORMAT(4005, "Email không đúng định dạng", HttpStatus.BAD_REQUEST),
    PASSWORD_SHOULD_NOT_BE_NULL(4006, "Mật khẩu không được để trống", HttpStatus.BAD_REQUEST),
    EMAIL_SHOULD_NOT_BE_NULL(4007, "Email không được để trống", HttpStatus.BAD_REQUEST),
    REFRESH_TOKEN_NOT_EXIST(4008, "Refresh token không tồn tại", HttpStatus.UNAUTHORIZED), // 401
    REFRESH_TOKEN_INVALID(4009, "Refresh token không hợp lệ", HttpStatus.UNAUTHORIZED), // 401
    REFRESH_TOKEN_EXPIRED(4010, "Refresh token đã hết hạn", HttpStatus.UNAUTHORIZED), // 401
    OLD_PASSWORD_SHOULD_NOT_BE_NULL(4011, "Mật khẩu cũ không được để trống", HttpStatus.BAD_REQUEST),
    NEW_PASSWORD_SHOULD_NOT_BE_NULL(4012, "Mật khẩu mới không được để trống", HttpStatus.BAD_REQUEST),
    CONFIRM_PASSWORD_SHOULD_NOT_BE_NULL(4013, "Xác nhận mật khẩu không được để trống", HttpStatus.BAD_REQUEST),
    PASSWORD_SHOULD_BE_AT_LEAST_8_CHARACTERS(4014, "Mật khẩu phải có ít nhất 8 ký tự", HttpStatus.BAD_REQUEST),
    PASSWORD_CONFIRMATION_MISMATCH(4015, "Mật khẩu mới và xác nhận mật khẩu không khớp", HttpStatus.BAD_REQUEST),
    USER_HAS_NO_LOCAL_PASSWORD(4016, "Người dùng không có mật khẩu cục bộ để thay đổi", HttpStatus.BAD_REQUEST),
    PASSWORD_INVALID(4017, "Mật khẩu cũ không đúng", HttpStatus.BAD_REQUEST),
    PASSWORD_IS_SAME_AS_OLD(4018, "Mật khẩu mới không được giống mật khẩu cũ", HttpStatus.BAD_REQUEST),
    NAME_SHOUT_NOT_BE_NULL(4019, "Mật khẩu không được để trống", HttpStatus.BAD_REQUEST),
    CANNOT_HASH_SHA256(4020, "Không thể băm chuỗi bằng SHA-256", HttpStatus.INTERNAL_SERVER_ERROR), // 500
    EMAIL_SENDING_FAILED(4021, "Gửi email thất bại", HttpStatus.INTERNAL_SERVER_ERROR), // 500
    ROLE_NOT_FOUND(4022, "Vai trò không tồn tại", HttpStatus.NOT_FOUND), // 404
    USER_NOT_ENABLED(4023, "Tài khoản chưa được kích hoạt. Vui lòng xác thực OTP.", HttpStatus.FORBIDDEN),
    REFRESH_TOKEN_EXPIRED_OR_NOT_EXIST(4024, "Refresh token đã hết hạn hoặc không tồn tại", HttpStatus.UNAUTHORIZED), // 401
    ACCESS_DENIED(4025, "Bạn không có quyền truy cập tài nguyên này", HttpStatus.FORBIDDEN), // 403
    USER_LOCKED(4026, "Tài khoản đã bị khóa", HttpStatus.FORBIDDEN),

    // 6. MODULE OTP - Range: 6000 -> 6999
    OTP_EXPIRED(6001, "Mã OTP đã hết hạn", HttpStatus.BAD_REQUEST),
    INVALID_OTP(6002, "Mã OTP không hợp lệ", HttpStatus.BAD_REQUEST),
    TOO_MANY_ATTEMPTS(6003, "Quá nhiều lần thử. Vui lòng yêu cầu mã OTP mới.", HttpStatus.TOO_MANY_REQUESTS), // 429
    OTP_SEND_TOO_FAST(6004, "Yêu cầu mã OTP quá nhanh. Vui lòng chờ trước khi thử lại.", HttpStatus.TOO_MANY_REQUESTS), // 429
    OTP_SHOULD_NOT_BE_NULL(6005, "Mã OTP không được để trống", HttpStatus.BAD_REQUEST),

    // 7. MODULE ASSET - Range: 7000 -> 7999
    ASSET_NOT_FOUND(7001, "Coin không tồn tại", HttpStatus.NOT_FOUND),

    // 5. MODULE FILE - Range: 5000 -> 5999
    FILE_NOT_FOUND(5001, "File không tồn tại", HttpStatus.NOT_FOUND), // 404
    FILE_UPLOAD_FAILED(5002, "Tải file lên thất bại", HttpStatus.INTERNAL_SERVER_ERROR), // 500
    FILE_TYPE_NOT_SUPPORTED(5003, "Loại file không được hỗ trợ", HttpStatus.UNSUPPORTED_MEDIA_TYPE), // 415
    FILE_SIZE_EXCEEDED(5004, "Kích thước file vượt quá giới hạn cho phép", HttpStatus.PAYLOAD_TOO_LARGE), // 413
    FILE_INVALID(5005, "File không hợp lệ", HttpStatus.BAD_REQUEST),
    FILE_IS_EMPTY(5006, "File rỗng", HttpStatus.BAD_REQUEST),
    FILE_TOO_LARGE(5007, "File quá lớn", HttpStatus.PAYLOAD_TOO_LARGE), // 413
    FILE_TOO_SMALL(5008, "File quá nhỏ", HttpStatus.BAD_REQUEST),
    FILE_READ_ERROR(5009, "Lỗi khi đọc file", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_MUST_BE_IMAGE(5010, "File phải là ảnh", HttpStatus.UNSUPPORTED_MEDIA_TYPE); // 415


    private final int code;
    private final String message;
    private final HttpStatus statusCode;

    ErrorCode(int code, String message, HttpStatus statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
}