package com.crypto.portfolio.repository;

import com.crypto.portfolio.entity.CryptoTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<CryptoTransaction, Long> {

    // Tìm tất cả giao dịch theo symbol
    List<CryptoTransaction> findBySymbol(String symbol);

    //Tim tất cả giao dịch theo userId
    List<CryptoTransaction> findAllByUserId(Long userId);
}
