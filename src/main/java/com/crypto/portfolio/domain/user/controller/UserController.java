package com.crypto.portfolio.domain.user.controller;

import com.crypto.portfolio.domain.user.dto.ChangePasswordRequestDTO;
import com.crypto.portfolio.domain.common.dto.ApiResponse;
import com.crypto.portfolio.domain.user.dto.UserResponseDTO;
import com.crypto.portfolio.security.user.CustomUserDetails;
import com.crypto.portfolio.domain.user.service.UserService;
import com.crypto.portfolio.domain.user.facade.UserFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserFacade userFacade;
    @PatchMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid ChangePasswordRequestDTO request
    ) {
        userService.changePassword(userDetails.getId(), request);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .code(1000)
                .message("Đổi mật khẩu thành công. Vui lòng đăng nhập lại.")
                .build());
    }

    @PostMapping("/upload-avatar")
    public ResponseEntity<ApiResponse<UserResponseDTO>> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {

        UserResponseDTO result = userFacade.uploadAvatar(file, customUserDetails.getId());

        return ResponseEntity.ok(ApiResponse.<UserResponseDTO>builder()
                .code(1000)
                .message("Cập nhật ảnh đại diện thành công.")
                .result(result)
                .build());
    }

}