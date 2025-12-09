package com.crypto.portfolio.controller;

import com.crypto.portfolio.dto.config.ApiResponse;
import com.crypto.portfolio.dto.portfolio.PortfolioResponseDTO;
import com.crypto.portfolio.dto.transactions.TransactionRequestDTO;
import com.crypto.portfolio.dto.transactions.TransactionResponseDTO;
import com.crypto.portfolio.entity.CryptoTransaction;
import com.crypto.portfolio.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    // API lấy danh sách giao dịch
    @GetMapping
    public ApiResponse<List<TransactionResponseDTO>> getAllTransaction() {
        List<TransactionResponseDTO> result = transactionService.getAllTransactions();

        return ApiResponse.<List<TransactionResponseDTO>>builder()
                .result(result)
                .message("Lấy danh sách thành công")
                .build();
    }

    // API thêm giao dịch mới
    @PostMapping
    public ApiResponse<TransactionResponseDTO> createTransaction(@Valid @RequestBody TransactionRequestDTO request) {
        TransactionResponseDTO result = transactionService.addTransaction(request);

        return ApiResponse.<TransactionResponseDTO>builder()
                .result(result)
                .message("Thêm giao dịch thành công")
                .build();
    }

    //API lấy danh sách symbol trong portfolio
    @GetMapping("/portfolio")
    public ApiResponse<List<PortfolioResponseDTO>> getPortfolio() {
        List<PortfolioResponseDTO> result = transactionService.getPortfolios();

        return ApiResponse.<List<PortfolioResponseDTO>>builder()
                .result(result)
                .message("Lấy danh sách portfolio thành công")
                .build();
    }
}
