package com.crypto.portfolio.security;

import com.crypto.portfolio.entity.User;
import com.crypto.portfolio.entity.UserAccount;
import com.crypto.portfolio.entity.type.AuthProvider;
import com.crypto.portfolio.repository.UserAccountRepository;
import com.crypto.portfolio.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
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

    // URL Frontend để redirect về sau khi login thành công
    // Trong thực tế nên để trong application.properties
    private final String FRONTEND_URL = "http://localhost:3000/oauth2/redirect";

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // 1. Lấy dữ liệu từ Google
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String avatar = oAuth2User.getAttribute("picture");
        String providerId = oAuth2User.getName(); // Đây là ID duy nhất của User trên Google (sub)
        AuthProvider provider = AuthProvider.GOOGLE;

        // 2. Tìm xem UserAccount này đã tồn tại chưa?
        UserAccount account = userAccountRepository.findByProviderAndProviderId(provider, providerId)
                .orElse(null);

        User user;

        if (account != null) {
            // CASE 1: Đã từng đăng nhập Google này rồi -> Lấy User ra
            user = account.getUser();

            // (Optional) Cập nhật lại Avatar/Tên nếu muốn data luôn mới
            // user.setAvatarUrl(avatar);
            // userRepository.save(user);
        } else {
            // CASE 2: Chưa từng đăng nhập Google này -> Check xem Email có trùng không
            Optional<User> existingUser = userRepository.findByEmail(email);

            if (existingUser.isPresent()) {
                // CASE 2.1: Email trùng với 1 user cũ -> LINK ACCOUNT (Gộp)
                user = existingUser.get();

                // Cập nhật avatar nếu user cũ chưa có
                if (user.getAvatarUrl() == null) {
                    user.setAvatarUrl(avatar);
                    userRepository.save(user);
                }
            } else {
                // CASE 2.2: User mới tinh -> TẠO USER MỚI
                User newUser = User.builder()
                        .email(email)
                        .fullName(name)
                        .avatarUrl(avatar)
                        .build();
                user = userRepository.save(newUser);
            }

            // TẠO LIÊN KẾT (Credential) cho cả Case 2.1 và 2.2
            UserAccount newAccount = UserAccount.builder()
                    .user(user)
                    .provider(provider)
                    .providerId(providerId)
                    .password(null) // Google không cần pass
                    .build();
            userAccountRepository.save(newAccount);
        }

        // 3. Tạo JWT Token
        String token = jwtUtils.generateToken(user.getEmail());

        // 4. Redirect về Frontend kèm Token
        String targetUrl = FRONTEND_URL + "?token=" + token;
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}