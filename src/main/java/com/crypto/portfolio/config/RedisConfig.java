package com.crypto.portfolio.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;

@Configuration
public class RedisConfig {

    @Bean
    public DefaultRedisScript<String> saveOtpScript() {
        DefaultRedisScript<String> script = new DefaultRedisScript<>();
        // Load file từ resources
        script.setLocation(new ClassPathResource("scripts/save_otp.lua"));
        script.setResultType(String.class);
        return script;
    }

    @Bean
    public DefaultRedisScript<String> validateOtpScript() {
        DefaultRedisScript<String> script = new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource("scripts/validate_otp.lua"));
        script.setResultType(String.class);
        return script;
    }

    @Bean
    public DefaultRedisScript<String> resendOtpScript() {
        DefaultRedisScript<String> script = new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource("scripts/resend_otp.lua"));
        script.setResultType(String.class);
        return script;
    }
}