package com.crypto.portfolio.service.serviceImpl;

import com.crypto.portfolio.annotation.Storage;
import com.crypto.portfolio.dto.file.FileUploadResponseDTO;
import com.crypto.portfolio.service.StorageService;
import com.crypto.portfolio.constants.StorageProvider;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Storage(StorageProvider.AWS_S3)
public class AwsS3ServiceImpl implements StorageService {
    @Override
    public FileUploadResponseDTO uploadFile(MultipartFile file, String uniqueFileName, String folderName) {
        return null;
    }

    @Override
    public void deleteFile(String fileId) {

    }
}
