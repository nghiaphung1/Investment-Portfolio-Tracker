package com.crypto.portfolio.domain.asset.repository;

import com.crypto.portfolio.domain.asset.entity.Asset;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AssetRepository extends JpaRepository<Asset, String> {
    // Khi gọi hàm này:
    // 1. Spring kiểm tra trong hộp cache tên "assets" xem có key là symbol chưa.
    // 2. Nếu có -> Trả về ngay (không gọi DB).
    // 3. Nếu chưa -> Gọi DB -> Lưu kết quả vào cache -> Trả về.
    @Cacheable(value = "assets", key = "#symbol")
    Optional<Asset> findBySymbol(String symbol);
}
