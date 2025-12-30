package com.crypto.portfolio.service.facade.facadeImpl;

import com.crypto.portfolio.annotation.Storage;
import com.crypto.portfolio.config.FileUploadConfig;
import com.crypto.portfolio.dto.file.FileUploadResponseDTO;
import com.crypto.portfolio.dto.users.UserResponseDTO;
import com.crypto.portfolio.entity.User;
import com.crypto.portfolio.exception.AppException;
import com.crypto.portfolio.exception.ErrorCode;
import com.crypto.portfolio.mapper.UserMapper;
import com.crypto.portfolio.service.StorageService;
import com.crypto.portfolio.service.UserService;
import com.crypto.portfolio.service.facade.UserFacade;
import com.crypto.portfolio.type.StorageProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserFacadeImpl implements UserFacade {
    @Storage(StorageProvider.CLOUDINARY)
    private final StorageService storageService;
    private final UserMapper userMapper;
    private final UserService userService;
    @Qualifier("fileTaskExecutor")
    private final Executor fileTaskExecutor;
    @Value("${cloudinary.folder.avatars}")
    private String avatarFolder;
    private final FileUploadConfig fileUploadConfig;

    @Override
    public UserResponseDTO uploadAvatar(MultipartFile file, Long userId) {
        checkValidImageFile(file);

        User user = userService.getByUserId(userId);

        String oldFileId = user.getAvatarFileId();

        // Tạo tên file độc nhất
        String uniqueFileName = "user_" + userId + "_" + UUID.randomUUID().toString();

        //  Upload ảnh mới
        FileUploadResponseDTO uploadResult = storageService.uploadFile(file, uniqueFileName, avatarFolder);

        try {
            user = userService.updateAvatar(user, uploadResult);
        }
        catch (Exception e) {
            try {
                storageService.deleteFile(uploadResult.getFileId());
            } catch (Exception ex) {
                log.error("Fail to delete fileId: {}", uploadResult.getFileId());
            }
            throw new AppException(ErrorCode.FILE_UPLOAD_FAILED);
        }

        // Sử dụng executor riêng để xóa file
        if (oldFileId != null && !oldFileId.isBlank()) {
            CompletableFuture.runAsync(
                    () -> storageService.deleteFile(oldFileId),
                    fileTaskExecutor
            );
        }

        return userMapper.toUserResponse(user);
    }

    private void checkValidImageFile(MultipartFile file) {
        // Check null/empty
        if (file == null || file.isEmpty()) {
            throw new AppException(ErrorCode.FILE_IS_EMPTY);
        }

        // Check kích thước (Max 5MB)
        if (file.getSize() > fileUploadConfig.getAvatar().toBytes()) {
            throw new AppException(ErrorCode.FILE_TOO_LARGE);
        }

        // Check xem phải ảnh thật không
        try {
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image == null) {
                throw new AppException(ErrorCode.FILE_MUST_BE_IMAGE);
            }
        } catch (IOException e) {
            throw new AppException(ErrorCode.FILE_INVALID);
        }
    }
}
