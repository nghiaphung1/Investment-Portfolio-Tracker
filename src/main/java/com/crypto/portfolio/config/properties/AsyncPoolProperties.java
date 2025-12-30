package com.crypto.portfolio.config.properties;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

public interface AsyncPoolProperties {
    int getCoreSize();
    int getMaxSize();
    int getQueueCapacity();
    String getPrefix();

    default void customize(ThreadPoolTaskExecutor executor) {
    }
}
