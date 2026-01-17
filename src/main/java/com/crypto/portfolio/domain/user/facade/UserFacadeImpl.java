package com.crypto.portfolio.domain.user.facade;

import com.crypto.portfolio.annotation.Facade;
import com.crypto.portfolio.annotation.Storage;
import com.crypto.portfolio.config.FileUploadConfig;
import com.crypto.portfolio.domain.user.dto.ChangePasswordRequestDTO;
import com.crypto.portfolio.domain.user.service.command.UserCommandService;
import com.crypto.portfolio.domain.user.service.query.UserQueryService;
import com.crypto.portfolio.infrastructure.storage.dto.FileUploadResponseDTO;
import com.crypto.portfolio.domain.user.dto.UserResponseDTO;
import com.crypto.portfolio.domain.user.entity.User;
import com.crypto.portfolio.exception.AppException;
import com.crypto.portfolio.exception.ErrorCode;
import com.crypto.portfolio.domain.user.mapper.UserMapper;
import com.crypto.portfolio.application.ports.output.StorageService;
import com.crypto.portfolio.constants.StorageProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Facade
@RequiredArgsConstructor
@Slf4j
public class UserFacadeImpl implements UserFacade {
    @Storage(StorageProvider.CLOUDINARY)
    private final StorageService storageService;
    private final UserMapper userMapper;
    private final UserQueryService userQueryService;
    private final UserCommandService userCommandService;
    @Qualifier("fileTaskExecutor")
    private final Executor fileTaskExecutor;
    @Value("${cloudinary.folder.avatars}")
    private String avatarFolder;
    private final FileUploadConfig fileUploadConfig;

    @Override
    public UserResponseDTO uploadAvatar(MultipartFile file, Long userId) {
        checkValidImageFile(file);

        User user = userQueryService.getByUserId(userId);

        String oldFileId = user.getAvatarFileId();

        // Tạo tên file độc nhất
        String uniqueFileName = "user_" + userId + "_" + UUID.randomUUID().toString();

        //  Upload ảnh mới
        FileUploadResponseDTO uploadResult = storageService.uploadFile(file, uniqueFileName, avatarFolder);

        try {
            user = userCommandService.updateAvatar(user, uploadResult.getUrl(), uploadResult.getFileId());
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

    @Override
    public void changePassword(Long userId, ChangePasswordRequestDTO request) {
        if(!request.getNewPassword().equals(request.getConfirmPassword())){
            throw new AppException(ErrorCode.PASSWORD_CONFIRMATION_MISMATCH);
        }
        userCommandService.changePassword(userId, request.getOldPassword(), request.getNewPassword());
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
