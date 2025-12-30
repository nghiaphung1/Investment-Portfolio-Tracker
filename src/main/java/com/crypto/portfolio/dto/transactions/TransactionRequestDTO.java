package com.crypto.portfolio.dto.transactions;

import com.crypto.portfolio.type.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data // Lombok tự sinh Getter/Setter
public class TransactionRequestDTO {

    @NotBlank(message = "INVALID_SYMBOL")
    private String symbol;

    @NotNull(message = "INVALID_QUANTITY")
    @DecimalMin(value = "0.00000001", message = "QUANTITY_MUST_BE_POSITIVE")
    private BigDecimal quantity;

    @NotNull(message = "INVALID_PRICE_PER_COIN")
    @DecimalMin(value = "0.0", message = "PRICE_CANNOT_BE_NEGATIVE")
    private BigDecimal pricePerCoin;

    @NotNull(message = "INVALID_TRANSACTION_TYPE")
    private TransactionType type;

    // Ngày giao dịch (có thể null, nếu null thì Service tự lấy giờ hiện tại)
    private LocalDateTime transactionDate;
}
