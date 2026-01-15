package com.crypto.portfolio.security.user;

import com.crypto.portfolio.entity.User;
import com.crypto.portfolio.entity.UserAccount;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.*;

@Getter
@Setter
@AllArgsConstructor
public class CustomUserDetails implements UserDetails, OAuth2User, CredentialsContainer {

    private static final long serialVersionUID = 1L; // Đảm bảo tính nhất quán khi serialize

    private Long id;
    private String email;
    private String fullName;
    private String avatar;

    @JsonIgnore
    private String password;

    private boolean enabled;

    private Collection<? extends GrantedAuthority> authorities;
    private Map<String, Object> attributes; // Dùng cho OAuth2 (Google/FB)

    private static Collection<? extends GrantedAuthority> mapRolesAndPermissionsToAuthorities(User user) {
        Set<GrantedAuthority> authorities = new HashSet<>();

        user.getRoles().forEach(role -> {
            // 1. Thêm Role (Ví dụ: ROLE_ADMIN, ROLE_USER)
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));

            // 2. Thêm các Permission tương ứng của Role đó
            if (role.getPermissions() != null) {
                role.getPermissions().forEach(permission -> {
                    authorities.add(new SimpleGrantedAuthority(permission.getName()));
                });
            }
        });

        return authorities;
    }

    //  Hàm tạo cho LOCAL Login
    public static CustomUserDetails create(User user, UserAccount account) {
        return new CustomUserDetails(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getAvatarUrl(),
                (account != null) ? account.getPassword() : null,
                user.isEnabled(),
                mapRolesAndPermissionsToAuthorities(user),
                null
        );
    }

    // Hàm tạo cho OAUTH2 Login (Google/FB)
    public static CustomUserDetails create(User user, Map<String, Object> attributes) {
        return new CustomUserDetails(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getAvatarUrl(),
                null,
                user.isEnabled(),
                mapRolesAndPermissionsToAuthorities(user),
                attributes
        );
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    // Override các hàm của OAuth2User ---

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getName() {
        return String.valueOf(id);
    }

    // Override hàm xóa thông tin nhạy cảm

    @Override
    public void eraseCredentials() {
        this.password = null;
    }
}