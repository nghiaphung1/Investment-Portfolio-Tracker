package com.crypto.portfolio.security;

import com.crypto.portfolio.entity.User;
import com.crypto.portfolio.entity.UserAccount;
import com.crypto.portfolio.entity.type.AuthProvider;
import com.crypto.portfolio.repository.UserAccountRepository;
import com.crypto.portfolio.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserAccountRepository userAccountRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        //Tìm User theo email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy user: " + email));

        //Tìm password(đã hash) từ UserAccount (nếu là user Local)
        String password = userAccountRepository.findByUserIdAndProvider(user.getId(), AuthProvider.LOCAL)
                .map(UserAccount::getPassword)
                .orElse(""); // Nếu là user Google/FB thì coi như password rỗng
        // 2. Chuyển đổi từ User Entity của bạn -> UserDetails chuẩn của Spring Security
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(password)
                .authorities(Collections.emptyList()) // Tạm thời chưa phân quyền (Role) nên để rỗng
                .build();
    }
}