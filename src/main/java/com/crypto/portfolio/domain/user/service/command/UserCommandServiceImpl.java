package com.crypto.portfolio.domain.user.service.command;

import com.crypto.portfolio.infrastructure.storage.dto.FileUploadResponseDTO;
import com.crypto.portfolio.domain.user.entity.User;
import com.crypto.portfolio.domain.user.entity.UserAccount;
import com.crypto.portfolio.constants.AuthProvider;
import com.crypto.portfolio.exception.AppException;
import com.crypto.portfolio.exception.ErrorCode;
import com.crypto.portfolio.domain.user.repository.UserAccountRepository;
import com.crypto.portfolio.domain.user.repository.UserRepository;
import com.crypto.portfolio.domain.auth.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserCommandServiceImpl implements UserCommandService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;

    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) {

        // Check trùng password cũ
        if (newPassword.equals(oldPassword)) {
            throw new AppException(ErrorCode.PASSWORD_IS_SAME_AS_OLD);
        }

        // Tìm UserAccount loại LOCAL
        UserAccount userAccount = userAccountRepository.findByUserIdAndProvider(userId, AuthProvider.LOCAL)
                .orElseThrow(() -> new AppException(ErrorCode.USER_HAS_NO_LOCAL_PASSWORD));

        // Check Mật khẩu cũ
        if (!passwordEncoder.matches(oldPassword, userAccount.getPassword())) {
            throw new AppException(ErrorCode.PASSWORD_INVALID);
        }

        // Cập nhật mật khẩu mới
        userAccount.setPassword(passwordEncoder.encode(newPassword));
        userAccountRepository.save(userAccount);

        // Revoke token
        refreshTokenService.revokeAllUserTokens(userId);
    }

    @Override
    public User updateAvatar(User user, String url, String fileId) {
        user.setAvatarUrl(url);
        user.setAvatarFileId(fileId);
        return userRepository.save(user);
    }


}