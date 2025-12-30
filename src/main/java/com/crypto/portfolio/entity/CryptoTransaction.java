package com.crypto.portfolio.entity;

import com.crypto.portfolio.type.TransactionType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CryptoTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String symbol; // Ví dụ: "BTC", "ETH", "SOL" - Nên lưu in hoa

    // Quan trọng: Dùng BigDecimal cho tiền tệ và số lượng crypto
    // precision = 19, scale = 8 nghĩa là tối đa 19 số, 8 số sau dấu phẩy (chuẩn của Bitcoin)
    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal quantity; // Số lượng coin mua/bán (Ví dụ: 0.005 BTC)

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal pricePerCoin; // Giá tại thời điểm mua (Ví dụ: $65000.50)

    @Column(nullable = false)
    private LocalDateTime transactionDate; // Thời gian giao dịch

    @Enumerated(EnumType.STRING)
    private TransactionType type; // BUY hoặc SELL

    @ManyToOne(fetch = FetchType.LAZY) // Lazy để tối ưu hiệu năng
    @JoinColumn(name = "user_id", nullable = false)      // Tên cột trong DB sẽ là user_id
    private User user;

    // Có thể thêm field 'fee' (phí giao dịch) sau này
}
