package com.crypto.portfolio.entity;

import com.crypto.portfolio.type.AuthProvider;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_accounts") // Bảng Credentials
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAccount extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthProvider provider; // LOCAL, GOOGLE, FACEBOOK

    // Nếu là Google/FB thì đây là ID của họ (sub). Nếu Local thì để null hoặc trùng email.

    private String providerId;

    // Chỉ dùng cho LOCAL, còn lại là Null
    private String password;

    // Liên kết với User gốc
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
