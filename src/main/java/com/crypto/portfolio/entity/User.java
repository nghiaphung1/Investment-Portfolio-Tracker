package com.crypto.portfolio.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users") // Bảng Identity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email; // Định danh chính (Email không được trùng)

    private String fullName;

    private String avatarUrl;

    // Một User có nhiều tài khoản đăng nhập (Local, Google, FB...)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<UserAccount> accounts = new ArrayList<>();
}