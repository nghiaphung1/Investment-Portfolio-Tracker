package com.crypto.portfolio.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.async.mail")
@Getter
@Setter
public class MailAsyncProperties implements AsyncPoolProperties{
    private int coreSize;
    private int maxSize;
    private int queueCapacity;
    private String prefix;
}
