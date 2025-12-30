package com.crypto.portfolio.controller;

import com.crypto.portfolio.dto.users.ChangePasswordRequestDTO;
import com.crypto.portfolio.dto.config.ApiResponse;
import com.crypto.portfolio.dto.users.UserResponseDTO;
import com.crypto.portfolio.security.CustomUserDetails;
import com.crypto.portfolio.service.UserService;
import com.crypto.portfolio.service.facade.UserFacade;
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