package com.crypto.portfolio.utils;

import com.crypto.portfolio.exception.AppException;
import com.crypto.portfolio.exception.ErrorCode;
import lombok.experimental.UtilityClass;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

@UtilityClass // Lombok tự động tạo class final và private constructor
public class HashUtils {

    public String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (Exception e) {
            throw new AppException(ErrorCode.CANNOT_HASH_SHA256);
        }
    }


}

