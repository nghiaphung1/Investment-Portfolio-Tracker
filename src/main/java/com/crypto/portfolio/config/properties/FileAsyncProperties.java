package com.crypto.portfolio.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.async.file")
@Getter
@Setter
public class FileAsyncProperties implements AsyncPoolProperties {
    private int coreSize;
    private int maxSize;
    private int queueCapacity;
    private String prefix;


}