package com.crypto.portfolio.domain.transaction.service.command;

import com.crypto.portfolio.domain.transaction.dto.TransactionResponseDTO;
import com.crypto.portfolio.domain.transaction.entity.CryptoTransaction;
import com.crypto.portfolio.domain.transaction.model.CreateTransactionCommand;

public interface TransactionCommandService {
    CryptoTransaction addTransaction(CreateTransactionCommand request);
}
