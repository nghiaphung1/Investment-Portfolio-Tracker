package com.crypto.portfolio.domain.transaction.service.query;

import com.crypto.portfolio.domain.transaction.dto.TransactionResponseDTO;
import com.crypto.portfolio.domain.transaction.dto.portfolio.PortfolioResponseDTO;
import com.crypto.portfolio.domain.transaction.entity.CryptoTransaction;
import com.crypto.portfolio.domain.transaction.repository.TransactionRepository;
import com.crypto.portfolio.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionQueryServiceImpl implements TransactionQueryService {
    private final TransactionRepository transactionRepository;

    @Override
    public List<CryptoTransaction> getAllTransactions(Long userId) {
        return transactionRepository.findByUserId(userId);
    }
}
