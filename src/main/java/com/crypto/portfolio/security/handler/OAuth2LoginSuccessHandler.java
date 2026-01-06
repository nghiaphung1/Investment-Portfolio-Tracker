package com.crypto.portfolio.security.handler;

import com.crypto.portfolio.entity.User;
import com.crypto.portfolio.entity.UserAccount;
import com.crypto.portfolio.type.AuthProvider;
import com.crypto.portfolio.repository.UserAccountRepository;
import com.crypto.portfolio.repository.UserRepository;
import com.crypto.portfolio.utils.JwtUtils;
import com.crypto.portfolio.service.serviceImpl.RefreshTokenServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    private final UserAccountRepository userAccountRepository;
    private final RefreshTokenServiceImpl refreshTokenService;

    @Value("${app.oauth2.redirect-url}")
    private String FRONTEND_URL;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        OAuth2AuthenticationToken authToken = (OAuth2AuthenticationToken) authentication;

        // 2. Xác định Provider (Google hay Facebook) động
        String registrationId = authToken.getAuthorizedClientRegistrationId();
        AuthProvider provider = "google".equals(registrationId) ? AuthProvider.GOOGLE : AuthProvider.FACEBOOK;

        // Lấy dữ liệu từ Google/FB
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String avatar;

        // Xử lý avatar (Google thì trường 'picture', FB thì cấu trúc khác nên tạm check null)
        if ("google".equals(registrationId)) {
            avatar = oAuth2User.getAttribute("picture");
        } else {
            // Logic lấy ảnh FB phức tạp hơn, tạm để null hoặc bạn tự xử lý sau
            avatar = null;
        }

        String providerId = oAuth2User.getName(); // ID duy nhất (sub)

        // Xử lý trường hợp Facebook không trả về email (đăng ký bằng SĐT)
        if (email == null) {
            email = providerId + "@" + registrationId + ".placeholder";
        }

        // --- LOGIC TÌM/TẠO USER (GIỮ NGUYÊN NHƯ CŨ) ---
        UserAccount account = userAccountRepository.findByProviderAndProviderId(provider, providerId)
                .orElse(null);

        User user;

        if (account != null) {
            // CASE 1: Đã có tài khoản
            user = account.getUser();
            // Cập nhật avatar nếu muốn
            if (avatar != null && !avatar.equals(user.getAvatarUrl())) {
                user.setAvatarUrl(avatar);
                userRepository.save(user);
            }
        } else {
            // CASE 2: Chưa có tài khoản
            Optional<User> existingUser = userRepository.findByEmail(email);

            if (existingUser.isPresent()) {
                // CASE 2.1: Trùng email -> Gộp
                user = existingUser.get();
                if (user.getAvatarUrl() == null) {
                    user.setAvatarUrl(avatar);
                    userRepository.save(user);
                }
            } else {
                // CASE 2.2: User mới tinh -> Tạo mới
                User newUser = User.builder()
                        .email(email)
                        .fullName(name)
                        .avatarUrl(avatar)
                        .build();
                user = userRepository.save(newUser);
            }

            // Tạo liên kết Credential
            UserAccount newAccount = UserAccount.builder()
                    .user(user)
                    .provider(provider)
                    .providerId(providerId)
                    .password(null)
                    .build();
            userAccountRepository.save(newAccount);
        }

        // --- PHẦN MỚI: TẠO COOKIE (Thay vì trả về URL Param) ---

        // 3. Tạo Access Token & Cookie
        String accessToken = jwtUtils.generateToken(user.getEmail());

        // 4. Tạo Refresh Token & Cookie (Gọi service tạo và lưu hash vào DB)
        String refreshTokenRaw = refreshTokenService.createRefreshToken(user.getId(), "Abc", "Abc");
        ResponseCookie refreshCookie = jwtUtils.generateRefreshTokenCookie(refreshTokenRaw);

        // 5. Gán Cookie vào Response Header
        // Lưu ý: Dùng addHeader để thêm 2 dòng Set-Cookie riêng biệt
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        // 6. Redirect về Frontend (URL sạch sẽ)
        // Frontend tự động nhận cookie, không cần parse từ URL nữa
        getRedirectStrategy().sendRedirect(request, response, FRONTEND_URL);
    }
}