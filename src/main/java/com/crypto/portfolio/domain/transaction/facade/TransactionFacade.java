package com.crypto.portfolio.domain.transaction.facade;

import com.crypto.portfolio.domain.transaction.dto.CreateTransactionRequestDTO;
import com.crypto.portfolio.domain.transaction.dto.TransactionResponseDTO;

import java.util.List;

public interface TransactionFacade {
    List<TransactionResponseDTO> getAllTransactions(Long userId);

    TransactionResponseDTO addTransaction(CreateTransactionRequestDTO request, Long userId);

}
