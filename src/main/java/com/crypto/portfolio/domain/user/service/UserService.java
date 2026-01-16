package com.crypto.portfolio.domain.user.service;

import com.crypto.portfolio.domain.user.dto.ChangePasswordRequestDTO;
import com.crypto.portfolio.infrastructure.storage.dto.FileUploadResponseDTO;
import com.crypto.portfolio.domain.user.entity.User;

public interface UserService {
    void changePassword(Long userId, ChangePasswordRequestDTO request);

    User updateAvatar(User user, FileUploadResponseDTO uploadResult);

    User getByUserId(Long userId);
}
