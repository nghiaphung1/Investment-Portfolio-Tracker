package com.crypto.portfolio.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app.upload.limits") // Trỏ đúng đường dẫn trong YAML
public class FileUploadConfig {

    // Tên biến phải trùng với key trong YAML (avatar, document, video)
    private DataSize avatar;
    private DataSize document;
    private DataSize video;
}
