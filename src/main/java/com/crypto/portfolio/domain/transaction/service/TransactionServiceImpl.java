package com.crypto.portfolio.domain.transaction.service;

import com.crypto.portfolio.constants.TransactionType;
import com.crypto.portfolio.domain.transaction.dto.portfolio.PortfolioResponseDTO;
import com.crypto.portfolio.domain.transaction.dto.TransactionRequestDTO;
import com.crypto.portfolio.domain.transaction.dto.TransactionResponseDTO;
import com.crypto.portfolio.domain.asset.entity.Asset;
import com.crypto.portfolio.domain.transaction.entity.CryptoTransaction;
import com.crypto.portfolio.domain.user.entity.User;
import com.crypto.portfolio.exception.AppException;
import com.crypto.portfolio.exception.ErrorCode;
import com.crypto.portfolio.domain.transaction.mapper.TransactionMapper;
import com.crypto.portfolio.domain.asset.repository.AssetRepository;
import com.crypto.portfolio.domain.transaction.repository.TransactionRepository;
import com.crypto.portfolio.domain.user.repository.UserRepository;
import com.crypto.portfolio.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AssetRepository assetRepository;
    private final UserRepository userRepository;
    private final TransactionMapper mapper;

    @Override
    @Transactional(readOnly = true) // Tối ưu hiệu năng cho thao tác đọc
    public List<TransactionResponseDTO> getAllTransactions() {
        Long userId = SecurityUtils.getCurrentUserId();
        // Nên dùng method có @EntityGraph trong Repository để tránh lỗi N+1
        List<CryptoTransaction> listTransaction = transactionRepository.findByUserId(userId);
        return mapper.toResponseList(listTransaction);
    }

    @Override
    @Transactional
    public TransactionResponseDTO addTransaction(TransactionRequestDTO request) {
        Long userId = SecurityUtils.getCurrentUserId();

        // 1. Fetch User & Asset
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Asset asset = assetRepository.findById(request.getSymbol().toUpperCase())
                .orElseThrow(() -> new AppException(ErrorCode.ASSET_NOT_FOUND));

        // 2. Build Entity (Dùng Builder pattern nếu Entity có @Builder, hoặc dùng Setter như cũ đều được)
        // Ở đây giữ nguyên logic Setter của bạn cho dễ hiểu
        CryptoTransaction transaction = mapper.toEntity(request);
        transaction.setUser(user);
        transaction.setAsset(asset);

        // Logic thời gian
        if (transaction.getTransactionDate() == null) {
            transaction.setTransactionDate(LocalDateTime.now());
        }

        // 3. Save & Return
        CryptoTransaction savedTransaction = transactionRepository.save(transaction);
        return mapper.toResponse(savedTransaction);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PortfolioResponseDTO> getPortfolios() {
        Long userId = SecurityUtils.getCurrentUserId();

        // 1. QUAN TRỌNG: Chỉ lấy data của User hiện tại
        // Lưu ý: Cần đảm bảo Repository dùng JOIN FETCH Asset để tránh N+1
        List<CryptoTransaction> listTransaction = transactionRepository.findByUserId(userId);

        if (listTransaction.isEmpty()) {
            return new ArrayList<>();
        }

        // 2. Grouping: Sửa lỗi gọi getSymbol() trực tiếp
        // Map<Symbol, List<Transaction>>
        Map<String, List<CryptoTransaction>> groupBySymbol = listTransaction.stream()
                .collect(Collectors.groupingBy(tx -> tx.getAsset().getSymbol()));

        List<PortfolioResponseDTO> listPortfolioResponse = new ArrayList<>();

        // 3. Calculation Loop
        for (Map.Entry<String, List<CryptoTransaction>> entry : groupBySymbol.entrySet()) {
            String symbol = entry.getKey();
            List<CryptoTransaction> transactions = entry.getValue();

            PortfolioResponseDTO dto = calculateCoinPortfolio(symbol, transactions);

            // Chỉ trả về những coin còn số dư > 0
            if (dto.getQuantity().compareTo(BigDecimal.ZERO) > 0) {
                listPortfolioResponse.add(dto);
            }
        }
        return listPortfolioResponse;
    }

    /**
     * Tách logic tính toán ra method riêng (Clean Code)
     */
    private PortfolioResponseDTO calculateCoinPortfolio(String symbol, List<CryptoTransaction> listTransactions) {
        BigDecimal totalQuantityBought = BigDecimal.ZERO; // Tổng số lượng mua vào
        BigDecimal totalCostBought = BigDecimal.ZERO;     // Tổng tiền bỏ ra mua
        BigDecimal currentQuantity = BigDecimal.ZERO;     // Số lượng hiện tại đang giữ

        for (CryptoTransaction tx : listTransactions) {
            if (tx.getType() == TransactionType.BUY) {
                totalQuantityBought = totalQuantityBought.add(tx.getQuantity());
                currentQuantity = currentQuantity.add(tx.getQuantity());

                // Tiền = Giá * Số lượng
                BigDecimal cost = tx.getPricePerCoin().multiply(tx.getQuantity());
                totalCostBought = totalCostBought.add(cost);

            } else if (tx.getType() == TransactionType.SELL) {
                currentQuantity = currentQuantity.subtract(tx.getQuantity());
            }
        }

        // Tính giá trung bình mua (Average Buy Price)
        // Công thức: Tổng tiền mua / Tổng số lượng mua
        BigDecimal avgBuyPrice = BigDecimal.ZERO;
        if (totalQuantityBought.compareTo(BigDecimal.ZERO) > 0) {
            avgBuyPrice = totalCostBought.divide(totalQuantityBought, 8, RoundingMode.HALF_UP);
        }

        // Tính giá trị vốn gốc hiện tại (Current Investment Cost)
        // Ví dụ: Mua giá TB 50k, đang giữ 2 BTC -> Vốn đang chôn là 100k
        BigDecimal currentInvestment = avgBuyPrice.multiply(currentQuantity);

        // Xử lý case số lượng <= 0 (đã bán hết)
        if (currentQuantity.compareTo(BigDecimal.ZERO) <= 0) {
            currentQuantity = BigDecimal.ZERO;
            currentInvestment = BigDecimal.ZERO;
        }

        return PortfolioResponseDTO.builder()
                .symbol(symbol)
                .quantity(currentQuantity)
                .averageBuyPrice(avgBuyPrice)
                .currentInvestment(currentInvestment)
                .build();
    }
}