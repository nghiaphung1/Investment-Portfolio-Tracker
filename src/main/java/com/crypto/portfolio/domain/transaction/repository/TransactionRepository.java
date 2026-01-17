package com.crypto.portfolio.domain.transaction.repository;

import com.crypto.portfolio.domain.transaction.entity.CryptoTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<CryptoTransaction, Long> {

    List<CryptoTransaction> findByUserId(Long userId);
}
