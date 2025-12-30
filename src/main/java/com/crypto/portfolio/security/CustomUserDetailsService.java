package com.crypto.portfolio.security;

import com.crypto.portfolio.entity.User;
import com.crypto.portfolio.entity.UserAccount;
import com.crypto.portfolio.type.AuthProvider;
import com.crypto.portfolio.repository.UserAccountRepository;
import com.crypto.portfolio.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserAccountRepository userAccountRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 1. Tìm User (Identity)
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // 2. Tìm Account (Credential - LOCAL) để lấy Password
        UserAccount account = userAccountRepository.findByUserIdAndProvider(user.getId(), AuthProvider.LOCAL)
                .orElseThrow(() -> new UsernameNotFoundException("User account not found"));

        // 3. Trả về CustomUserDetails (Chứa đầy đủ ID, Avatar, Pass...)
        return CustomUserDetails.create(user, account);
    }
}