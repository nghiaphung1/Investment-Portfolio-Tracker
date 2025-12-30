package com.crypto.portfolio.service.serviceImpl;

import com.crypto.portfolio.dto.portfolio.PortfolioResponseDTO;
import com.crypto.portfolio.dto.transactions.TransactionRequestDTO;
import com.crypto.portfolio.dto.transactions.TransactionResponseDTO;
import com.crypto.portfolio.entity.CryptoTransaction;
import com.crypto.portfolio.type.TransactionType;
import com.crypto.portfolio.mapper.TransactionMapper;
import com.crypto.portfolio.repository.TransactionRepository;
import com.crypto.portfolio.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository repository;
    private final TransactionMapper mapper;

    // Hàm lấy list danh sách transactions
    @Override
    public List<TransactionResponseDTO> getAllTransactions() {
        List<CryptoTransaction> listTransaction = repository.findAll();
        // Dùng Mapper chuyển cả danh sách Entity -> DTO
        return mapper.toResponseList(listTransaction);
    }

    // Hàm thêm mới 1 transaction
    @Transactional
    @Override
    public TransactionResponseDTO addTransaction(TransactionRequestDTO request) {
        // Chuyển từ DTO -> Entity
        CryptoTransaction transaction = mapper.toEntity(request);

        // Nếu người dùng không set Date thì lấy Time hiện tại
        if (transaction.getTransactionDate() == null) {
            transaction.setTransactionDate(LocalDateTime.now());
        }
        transaction.setSymbol(transaction.getSymbol().toUpperCase());

        // Tạo 1 đối tượng rồi lưu vào DB để lấy ID của nó
        CryptoTransaction savedTransaction = repository.save(transaction);

        // Chuyển từ Entity đã lưu (có ID) -> DTO để trả về Controller
        return mapper.toResponse(savedTransaction);
    }

    //Hàm lấy list portfolio của 1 user
    @Override
    public List<PortfolioResponseDTO> getPortfolios() {
        //Lấy tất cả transaction ở DB
        List<CryptoTransaction> listTransaction = repository.findAll();

        //Gom các symbol thành 1 nhóm Map
        Map<String, List<CryptoTransaction>> groupBySymbol = listTransaction.stream()
                .collect(Collectors.groupingBy(CryptoTransaction::getSymbol));

        //Tạo 1 list portfolio rỗng
        List<PortfolioResponseDTO> listPortfolioResponse = new ArrayList<>();

        //For each từng entry trong Map
        for(Map.Entry<String, List<CryptoTransaction>> entry: groupBySymbol.entrySet()) {
            String symbol = entry.getKey();
            List<CryptoTransaction> listTransactions = entry.getValue();
            PortfolioResponseDTO dto = calculateCoinPortfolio(symbol, listTransactions); //chưa xử lý

            //BigDecimal là 1 Object nên không thể > 0 trực tiếp
            if(dto.getQuantity().compareTo(BigDecimal.ZERO) > 0){
                listPortfolioResponse.add(dto);
            }
        }
        return listPortfolioResponse;
    }

    private PortfolioResponseDTO calculateCoinPortfolio(String symbol, List<CryptoTransaction> listTransactions) {
        BigDecimal totalQuantity = BigDecimal.ZERO; //Tổng số lượng coin đã mua
        BigDecimal totalCost = BigDecimal.ZERO; //Tổng số tiền đã mua
        BigDecimal currentQuantity = BigDecimal.ZERO; //Số lượng coin hiện tại đang nắm giữ

        //Duyệt từng transaction (cùng 1 symbol)
        for(CryptoTransaction transaction: listTransactions){
            if(transaction.getType() == TransactionType.BUY){
                totalQuantity = totalQuantity.add(transaction.getQuantity()); //Thêm giá trị vào tổng số lượng đã mua
                currentQuantity = currentQuantity.add(transaction.getQuantity()); //Thêm giá trị vào số lượng coin hiện tại đang nắm giữ
                totalCost = totalCost.add(transaction.getPricePerCoin().multiply(transaction.getQuantity()));

            }else if(transaction.getType() == TransactionType.SELL){
                currentQuantity = currentQuantity.subtract(transaction.getQuantity());
            }
        }

        //Trung bình giá đồng coin đó người dùng mua vào //CT:Avg = Total Cost / Total Quantity
        BigDecimal avgPrice = BigDecimal.ZERO;
        if(totalQuantity.compareTo(BigDecimal.ZERO) > 0){
            avgPrice = totalCost.divide(totalQuantity, 8, RoundingMode.HALF_UP);
        }

        //Số tiền hiện tại đang đầu tư trong portfolio
        BigDecimal currentCost = avgPrice.multiply(currentQuantity);
        if(currentQuantity.compareTo(BigDecimal.ZERO) <= 0){
            currentCost = BigDecimal.ZERO;
        }

        return PortfolioResponseDTO.builder()
                .symbol(symbol)
                .quantity(currentQuantity)
                .averageBuyPrice(avgPrice)
                .currentInvestment(currentCost)
                .build();
    }
}