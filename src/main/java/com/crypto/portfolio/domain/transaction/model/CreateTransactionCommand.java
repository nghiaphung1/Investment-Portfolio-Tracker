package com.crypto.portfolio.domain.transaction.model;

import com.crypto.portfolio.constants.TransactionType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Builder
public class CreateTransactionCommand {
    private Long userId;
    private String symbol;
    private BigDecimal quantity;
    private BigDecimal pricePerCoin;
    private TransactionType type;
    private Instant transactionDate;
}
