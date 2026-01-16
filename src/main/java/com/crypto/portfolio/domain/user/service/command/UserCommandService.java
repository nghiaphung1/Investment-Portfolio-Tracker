package com.crypto.portfolio.domain.user.service.command;

import com.crypto.portfolio.infrastructure.storage.dto.FileUploadResponseDTO;
import com.crypto.portfolio.domain.user.entity.User;

public interface UserCommandService {
    void changePassword(Long userId, String oldPassword, String newPassword);

    User updateAvatar(User user, String url, String fileId);


}
