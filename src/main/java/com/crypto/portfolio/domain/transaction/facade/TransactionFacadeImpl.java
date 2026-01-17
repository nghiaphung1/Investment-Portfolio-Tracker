package com.crypto.portfolio.domain.transaction.facade;

import com.crypto.portfolio.annotation.Facade;
import com.crypto.portfolio.domain.transaction.dto.CreateTransactionRequestDTO;
import com.crypto.portfolio.domain.transaction.dto.TransactionResponseDTO;
import com.crypto.portfolio.domain.transaction.entity.CryptoTransaction;
import com.crypto.portfolio.domain.transaction.mapper.TransactionMapper;
import com.crypto.portfolio.domain.transaction.model.CreateTransactionCommand;
import com.crypto.portfolio.domain.transaction.service.command.TransactionCommandService;
import com.crypto.portfolio.domain.transaction.service.query.TransactionQueryService;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Facade
@RequiredArgsConstructor
public class TransactionFacadeImpl implements TransactionFacade {
    private final TransactionQueryService transactionQueryService;
    private final TransactionCommandService transactionCommandService;
    private final TransactionMapper transactionMapper;

    @Override
    public List<TransactionResponseDTO> getAllTransactions(Long userId) {
        List<CryptoTransaction> transactions = transactionQueryService.getAllTransactions(userId);
        return transactionMapper.toResponseList(transactions);
    }

    @Override
    public TransactionResponseDTO addTransaction(CreateTransactionRequestDTO request, Long userId) {
        CreateTransactionCommand command = transactionMapper.toCommand(userId, request);
        CryptoTransaction savedTransaction = transactionCommandService.addTransaction(command);
        return transactionMapper.toResponse(savedTransaction);
    }
}
