package com.crypto.portfolio.domain.user.entity;

import com.crypto.portfolio.domain.common.persistence.BaseEntity;
import com.crypto.portfolio.domain.iam.entity.Role;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email; // Định danh chính (Email không được trùng)

    private String fullName;

    private String avatarUrl;

    private String avatarFileId;

    @Builder.Default
    private boolean enabled = false;

    // Một User có nhiều tài khoản đăng nhập (Local, Google, FB...)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<UserAccount> accounts = new ArrayList<>();

    @ManyToMany(fetch = FetchType.EAGER) // Lấy luôn Role khi load User
    @JoinTable(
            name = "users_roles", // Tên bảng trung gian trong DB
            joinColumns = @JoinColumn(name = "user_id", nullable = false), // Khóa ngoại trỏ tới bảng users
            inverseJoinColumns = @JoinColumn(name = "role_id", nullable = false) // Khóa ngoại trỏ tới bảng roles
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    // Helper method để thêm Role cực kỳ quan trọng
    public void addRole(Role role) {
        this.getRoles().add(role);
        role.getUsers().add(this);
    }


    // Helper method để thêm UserAccount và gán user cho account cùng lúc
    public void addUserAccount(UserAccount account) {
        this.getAccounts().add(account);
        account.setUser(this); // Bước này cực kỳ quan trọng để lưu được user_id vào DB
    }
}