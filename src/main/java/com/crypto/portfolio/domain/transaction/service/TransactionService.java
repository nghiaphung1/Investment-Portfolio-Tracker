package com.crypto.portfolio.domain.transaction.service;

import com.crypto.portfolio.domain.transaction.dto.portfolio.PortfolioResponseDTO;
import com.crypto.portfolio.domain.transaction.dto.CreateTransactionRequestDTO;
import com.crypto.portfolio.domain.transaction.dto.TransactionResponseDTO;

import java.util.List;

public interface TransactionService {
    List<TransactionResponseDTO> getAllTransactions();

    TransactionResponseDTO addTransaction(CreateTransactionRequestDTO request);

    List<PortfolioResponseDTO> getPortfolios();

}
