package com.crypto.portfolio.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String refreshToken; // Chuỗi Token dài hạn

    @Column(nullable = false)
    private Instant expiryDate; // Thời điểm hết hạn

    @Column(nullable = false)
    private Instant createdAt;

    @Column(name = "user_agent")
    private String userAgent;

    // 3. Để truy vết bảo mật hoặc chặn IP lạ
    @Column(name = "ip_address")
    private String ipAddress;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
    }
}
