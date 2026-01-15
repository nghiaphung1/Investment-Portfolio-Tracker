package com.crypto.portfolio.service.serviceImpl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.crypto.portfolio.annotation.Storage;
import com.crypto.portfolio.dto.file.FileUploadResponseDTO;
import com.crypto.portfolio.exception.AppException;
import com.crypto.portfolio.exception.ErrorCode;
import com.crypto.portfolio.service.StorageService;
import com.crypto.portfolio.constants.StorageProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@Storage(StorageProvider.CLOUDINARY)
@RequiredArgsConstructor
@Slf4j

public class CloudinaryServiceImpl implements StorageService {

    private final Cloudinary cloudinary;

    /**
     * HÀM UPLOAD ẢNH LÊN CLOUDINARY
     * */
    @Override
    public FileUploadResponseDTO uploadFile(MultipartFile file, String uniqueName, String folderName) {
        //Validate file trước khi upload
        validateFileIsValid(file);

        try {
            // Upload
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "folder", folderName,
                    "public_id", uniqueName,
                    "resource_type", "auto"
            ));

            // Lấy public_id thật từ kết quả (có kèm folder prefix)
            String publicId = (String) uploadResult.get("public_id");
            String secureUrl = (String) uploadResult.get("secure_url");
            String format = (String) uploadResult.get("format");

            // Trả về cả 2 để Service gọi nó có thể lưu vào DB
            return FileUploadResponseDTO.builder()
                    .fileId(publicId)
                    .url(secureUrl)
                    .format(format)
                    .build();

        } catch (IOException e) {
            throw new AppException(ErrorCode.FILE_UPLOAD_FAILED, e);
        }
    }

    /**
     * Hàm xóa ảnh cũ (Dùng khi user update avatar)
     */

    @Override
    public void deleteFile(String fileId) {
        if (fileId == null || fileId.isEmpty()) return;

        try {
            cloudinary.uploader().destroy(fileId, ObjectUtils.emptyMap());
        } catch (IOException e) {
            log.error("Lỗi khi xóa ảnh với publicId: " + fileId, e);
        }
    }

    /**
     * Hàm validate file trước khi upload
     */
    private void validateFileIsValid(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new AppException(ErrorCode.FILE_IS_EMPTY);
        }
    }
}