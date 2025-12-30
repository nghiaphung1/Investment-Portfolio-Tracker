package com.crypto.portfolio.service;

import com.crypto.portfolio.dto.file.FileUploadResponseDTO;
import org.springframework.web.multipart.MultipartFile;

public interface StorageService {

    FileUploadResponseDTO uploadFile(MultipartFile file, String uniqueFileName, String folderName);

    void deleteFile(String fileId);
}
