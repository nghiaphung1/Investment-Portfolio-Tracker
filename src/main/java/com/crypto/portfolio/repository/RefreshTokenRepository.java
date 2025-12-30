package com.crypto.portfolio.repository;

import com.crypto.portfolio.entity.RefreshToken;
import com.crypto.portfolio.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByRefreshToken(String token);

    // Lấy danh sách token của user, sắp xếp Tăng dần theo ngày tạo (Cũ nhất -> Mới nhất)
    List<RefreshToken> findAllByUserIdOrderByCreatedAtAsc(Long userId);


    //Xóa theo Token (Custom Query để tối ưu 1 lệnh SQL)
    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.refreshToken = :token")
    void deleteByToken(String token);

    // Xóa tất cả token của 1 user (Dùng cho Revoke All)
    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.user.id = :userId")
    void deleteByUserId(Long userId);

    // Xóa các token đã hết hạn (Dùng cho Cron Job)
    // Xóa trực tiếp bằng SQL, không cần load entity lên RAM
    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.expiryDate < :now")
    void deleteByExpiryDateBefore(@Param("now") Instant now);

//    @Modifying
//    @Query(value = """
//        INSERT INTO refresh_token (user_id, token, expiry_date)
//        VALUES (:userId, :token, :expiryDate)
//        ON CONFLICT (user_id) DO UPDATE
//        SET token = EXCLUDED.token,
//            expiry_date = EXCLUDED.expiry_date
//        """, nativeQuery = true)
//    void upsertToken(@Param("userId") Long userId,
//                     @Param("token") String token,
//                     @Param("expiryDate") Instant expiryDate);
}
