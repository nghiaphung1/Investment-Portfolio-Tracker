package com.crypto.portfolio.domain.asset.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "assets")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Asset {

    @Id
    @Column(length = 20, unique = true)
    private String symbol; // Khóa chính. VD: BTC, ETH, USDT, SOL

    @Column(nullable = false)
    private String name;   // Tên đầy đủ. VD: Bitcoin, Ethereum

    @Column(name = "coingecko_id", unique = true)
    private String coinGeckoId; // ID dùng để gọi API lấy giá. VD: bitcoin, ethereum

    @Column(name = "icon_url")
    private String iconUrl; // Link ảnh logo coin (lấy từ CoinGecko hoặc Cloudinary của bạn)

    @Builder.Default
    @Column(name = "is_active")
    private boolean isActive = true; // Admin có thể tắt coin này nếu không muốn hỗ trợ nữa
}