package com.crypto.portfolio.repository;

import com.crypto.portfolio.entity.UserAccount;
import com.crypto.portfolio.type.AuthProvider;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
    // Tìm xem cặp (Google + ID 12345) đã tồn tại chưa
    @EntityGraph(attributePaths = "user")
    Optional<UserAccount> findByProviderAndProviderId(AuthProvider provider, String providerId);

    // Tìm account Local để check pass (dùng cho API Login thường)
    @EntityGraph(attributePaths = "user")
    Optional<UserAccount> findByUserIdAndProvider(Long userId, AuthProvider provider);

}
