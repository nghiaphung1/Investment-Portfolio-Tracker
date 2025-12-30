package com.crypto.portfolio.config;

import com.crypto.portfolio.config.properties.AsyncPoolProperties;
import com.crypto.portfolio.config.properties.FileAsyncProperties;
import com.crypto.portfolio.config.properties.MailAsyncProperties;
import com.crypto.portfolio.exception.CustomAsyncExceptionHandler;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.jvm.ExecutorServiceMetrics;
import lombok.RequiredArgsConstructor;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync   // Kích hoạt tính năng chạy bất đồng bộ trong Spring
@EnableRetry
@RequiredArgsConstructor
public class AsyncConfig implements AsyncConfigurer {
    private final FileAsyncProperties fileProps;
    private final MailAsyncProperties mailProps;
    private final MeterRegistry meterRegistry;

    @Bean(name = "fileTaskExecutor")
    public Executor fileTaskExecutor() {
        return createExecutor(fileProps, "file_pool");
    }

    @Bean(name = "mailTaskExecutor")
    public Executor mailTaskExecutor() {
        return createExecutor(mailProps, "mail_pool");
    }

    private Executor createExecutor(AsyncPoolProperties props, String poolName) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(props.getCoreSize());
        executor.setMaxPoolSize(props.getMaxSize());
        executor.setQueueCapacity(props.getQueueCapacity());
        executor.setThreadNamePrefix(props.getPrefix());
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // Cực kỳ quan trọng: Đăng ký với MeterRegistry để theo dõi
        executor.initialize();

        return ExecutorServiceMetrics.monitor(
                meterRegistry,
                executor.getThreadPoolExecutor(),
                "executor_metrics", // Tên chung của metric
                Tags.of("name", poolName)    // TAG: key là "name", value là poolName (mail_pool/file_pool)
        );
    }

    @Override
    public Executor getAsyncExecutor() {
        return fileTaskExecutor(); // Mặc định dùng Pool File nếu chỉ ghi @Async
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new CustomAsyncExceptionHandler();
    }
}
