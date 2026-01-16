package com.crypto.portfolio.infrastructure.storage.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class FileUploadResponseDTO {
    private String fileId; // ID để xóa sau này
    private String url;      // Link hiển thị
    private String format;   // jpg, png...
}
