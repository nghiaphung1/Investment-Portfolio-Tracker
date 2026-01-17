package com.crypto.portfolio.domain.transaction.service.query;

import com.crypto.portfolio.domain.transaction.dto.TransactionResponseDTO;
import com.crypto.portfolio.domain.transaction.dto.portfolio.PortfolioResponseDTO;
import com.crypto.portfolio.domain.transaction.entity.CryptoTransaction;

import java.util.List;

public interface TransactionQueryService {
//    List<PortfolioResponseDTO> getPortfolios();

    List<CryptoTransaction> getAllTransactions(Long userId);
}
