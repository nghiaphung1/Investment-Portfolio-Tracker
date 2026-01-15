package com.crypto.portfolio.repository;

import com.crypto.portfolio.entity.Asset;
import com.crypto.portfolio.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AssetRepository extends JpaRepository<Asset, Long> {
    Optional<Asset> findById(String id);
}
