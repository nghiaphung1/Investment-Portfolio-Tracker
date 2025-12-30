package com.crypto.portfolio.service;

import com.crypto.portfolio.dto.portfolio.PortfolioResponseDTO;
import com.crypto.portfolio.dto.transactions.TransactionRequestDTO;
import com.crypto.portfolio.dto.transactions.TransactionResponseDTO;
import com.crypto.portfolio.entity.CryptoTransaction;

import java.util.List;

public interface TransactionService {
    List<TransactionResponseDTO> getAllTransactions();

    TransactionResponseDTO addTransaction(TransactionRequestDTO request);

    List<PortfolioResponseDTO> getPortfolios();

}
