package com.crypto.portfolio.dto.transactions;

import com.crypto.portfolio.entity.type.TransactionType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransactionResponseDTO {
    private Long id; // Quan trọng: Output phải có ID để FE quản lý
    private String symbol;
    private BigDecimal quantity;
    private BigDecimal pricePerCoin;
    private TransactionType type;
    private LocalDateTime transactionDate;
}
