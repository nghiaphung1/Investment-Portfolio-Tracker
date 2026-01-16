package com.crypto.portfolio.application.ports.output;

import com.crypto.portfolio.infrastructure.storage.dto.FileUploadResponseDTO;
import org.springframework.web.multipart.MultipartFile;

public interface StorageService {

    FileUploadResponseDTO uploadFile(MultipartFile file, String uniqueFileName, String folderName);

    void deleteFile(String fileId);
}
