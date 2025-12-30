package com.crypto.portfolio.utils.otp;

import com.crypto.portfolio.exception.AppException;
import com.crypto.portfolio.exception.ErrorCode;
import lombok.experimental.UtilityClass;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@UtilityClass
public class HashOtpUtils {
    private static final String HMAC_ALGO = "HmacSHA256";
    // Truyền salt (ví dụ: userId) vào để mỗi user có một hash khác nhau dù trùng OTP
    public String hmacSha256(String otp, String salt, String secretKey) {
        try {
            // 1. Secret Key chỉ đóng vai trò là "Chìa khóa"
            SecretKeySpec keySpec = new SecretKeySpec(
                    secretKey.getBytes(StandardCharsets.UTF_8),
                    HMAC_ALGO
            );

            Mac mac = Mac.getInstance(HMAC_ALGO);
            mac.init(keySpec);

            // 2. Message = OTP + Context (Salt)
            // Dùng dấu phân cách ":" để tránh xung đột dữ liệu (Canonicalization)
            String message = "OTP:" + otp + ":UID:" + salt;

            byte[] raw = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));

            return Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
        } catch (Exception e) {
            throw new AppException(ErrorCode.CANNOT_HASH_SHA256);
        }
    }
}
