package com.crypto.portfolio.dto.portfolio;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioResponseDTO {
    private String symbol;              // Tên coin (BTC)
    private BigDecimal quantity;        // Tổng số lượng đang giữ
    private BigDecimal averageBuyPrice; // Giá mua trung bình
    private BigDecimal currentInvestment; // Tổng tiền vốn đang bỏ ra
}
