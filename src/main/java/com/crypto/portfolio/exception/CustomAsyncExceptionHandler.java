package com.crypto.portfolio.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import java.lang.reflect.Method;

@Slf4j
public class CustomAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {


    @Override
    public void handleUncaughtException(Throwable ex, Method method, Object... params) {
        log.error("--- ASYNC ERROR ---");
        log.error("Thông báo lỗi: " + ex.getMessage());
        log.error("Tại phương thức: " + method.getName());
        for (Object param : params) {
            log.error("Tham số đầu vào: " + param);
        }
        log.error("--------------------");

        // Ở đây bạn có thể thêm logic: Gửi email cho Admin, push tin nhắn Slack/Telegram...
    }
}
