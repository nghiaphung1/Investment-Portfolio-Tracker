package com.crypto.portfolio.service.serviceImpl;

import com.crypto.portfolio.dto.users.ChangePasswordRequestDTO;
import com.crypto.portfolio.dto.file.FileUploadResponseDTO;
import com.crypto.portfolio.entity.User;
import com.crypto.portfolio.entity.UserAccount;
import com.crypto.portfolio.type.AuthProvider;
import com.crypto.portfolio.exception.AppException;
import com.crypto.portfolio.exception.ErrorCode;
import com.crypto.portfolio.repository.UserAccountRepository;
import com.crypto.portfolio.repository.UserRepository;
import com.crypto.portfolio.service.RefreshTokenService;
import com.crypto.portfolio.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;


    @Transactional
    @Override
    public void changePassword(Long userId, ChangePasswordRequestDTO request) {
        //  Check Confirm Password (Validation Logic)
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new AppException(ErrorCode.PASSWORD_CONFIRMATION_MISMATCH);
        }

        // Check trùng password cũ (Optional Validation)
        if (request.getNewPassword().equals(request.getOldPassword())) {
            throw new AppException(ErrorCode.PASSWORD_IS_SAME_AS_OLD);
        }

        // Tìm UserAccount loại LOCAL (Business Logic)
        UserAccount userAccount = userAccountRepository.findByUserIdAndProvider(userId, AuthProvider.LOCAL)
                .orElseThrow(() -> new AppException(ErrorCode.USER_HAS_NO_LOCAL_PASSWORD));

        // Check Mật khẩu cũ (Security Logic)
        if (!passwordEncoder.matches(request.getOldPassword(), userAccount.getPassword())) {
            throw new AppException(ErrorCode.PASSWORD_INVALID);
        }

        // Cập nhật mật khẩu mới (Persistence)
        userAccount.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userAccountRepository.save(userAccount);

        // Revoke token (Security Cleanup)
        refreshTokenService.revokeAllUserTokens(userId);
    }

    @Transactional
    @Override
    public User updateAvatar(User user, FileUploadResponseDTO uploadResult) {
        user.setAvatarUrl(uploadResult.getUrl());
        user.setAvatarFileId(uploadResult.getFileId());
        return userRepository.save(user);
    }

    @Override
    public User getByUserId(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }
}