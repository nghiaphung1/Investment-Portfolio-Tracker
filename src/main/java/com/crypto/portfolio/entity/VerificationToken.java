package com.crypto.portfolio.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "verification_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificationToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String otpCode;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private LocalDateTime expiryDate;

    @Builder.Default
    private int failedAttempts = 0;

    public VerificationToken(User user, String otpCode) {
        this.user = user;
        this.otpCode = otpCode;
        this.expiryDate = LocalDateTime.now().plusMinutes(5);
        this.failedAttempts = 0;
    }

    public static VerificationToken generateForUser(User user, String otpCode) {
        return VerificationToken.builder()
                .user(user)
                .otpCode(otpCode)
                .expiryDate(LocalDateTime.now().plusMinutes(5))
                .failedAttempts(0)
                .build();
    }

    public void resendhOtp(String newCode) {
        this.otpCode = newCode;
        this.expiryDate = LocalDateTime.now().plusMinutes(5);
        this.failedAttempts = 0;
    }

    // Kiểm tra hết hạn ngay trong Entity (Domain Logic)
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiryDate);
    }
}