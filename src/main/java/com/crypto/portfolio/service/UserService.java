package com.crypto.portfolio.service;

import com.crypto.portfolio.dto.users.ChangePasswordRequestDTO;
import com.crypto.portfolio.dto.file.FileUploadResponseDTO;
import com.crypto.portfolio.entity.User;

public interface UserService {
    void changePassword(Long userId, ChangePasswordRequestDTO request);

    User updateAvatar(User user, FileUploadResponseDTO uploadResult);

    User getByUserId(Long userId);
}
