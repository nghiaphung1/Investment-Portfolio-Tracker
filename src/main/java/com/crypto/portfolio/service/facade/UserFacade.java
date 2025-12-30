package com.crypto.portfolio.service.facade;

import com.crypto.portfolio.dto.users.UserResponseDTO;
import org.springframework.web.multipart.MultipartFile;

public interface UserFacade {
    UserResponseDTO uploadAvatar(MultipartFile file, Long userId);
}
