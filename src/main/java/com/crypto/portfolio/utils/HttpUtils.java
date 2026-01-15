package com.crypto.portfolio.utils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;

@UtilityClass
public class HttpUtils {

    private final String[] IP_HEADERS = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
            // Bạn cũng có thể thêm "CF-Connecting-IP" nếu dùng Cloudflare
    };

    public String getClientIp(HttpServletRequest request) {
        for (String header : IP_HEADERS) {
            String value = request.getHeader(header);
            if (value != null && value.length() > 0 && !"unknown".equalsIgnoreCase(value)) {
                // X-Forwarded-For có thể chứa nhiều IP (client, proxy1, proxy2...).
                // IP đầu tiên là IP thật của Client.
                return value.split(",")[0].trim();
            }
        }
        return request.getRemoteAddr();
    }
}