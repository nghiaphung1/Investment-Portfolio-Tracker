package com.crypto.portfolio.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Tạo các Serializer
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();

        // 1. Cấu hình cho Key (String)
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer); // <--- NÊN THÊM DÒNG NÀY

        // 2. Cấu hình cho Value (JSON)
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        // Khởi tạo (Best practice)
        template.afterPropertiesSet();

        return template;
    }

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