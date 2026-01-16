package com.crypto.portfolio.domain.user.facade;

import com.crypto.portfolio.domain.user.dto.UserResponseDTO;
import org.springframework.web.multipart.MultipartFile;

public interface UserFacade {
    UserResponseDTO uploadAvatar(MultipartFile file, Long userId);
}
