package com.crypto.portfolio.domain.transaction.controller;

import com.crypto.portfolio.domain.common.dto.ApiResponse;
import com.crypto.portfolio.domain.transaction.dto.CreateTransactionRequestDTO;
import com.crypto.portfolio.domain.transaction.dto.TransactionResponseDTO;
import com.crypto.portfolio.domain.transaction.facade.TransactionFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionFacade transactionFacade;

    // API lấy danh sách giao dịch
    @GetMapping
    public ApiResponse<List<TransactionResponseDTO>> getAllTransaction(@AuthenticationPrincipal Jwt jwt) {
        Long userId = Long.parseLong(jwt.getSubject());
        List<TransactionResponseDTO> result = transactionFacade.getAllTransactions(userId);

        return ApiResponse.<List<TransactionResponseDTO>>builder()
                .result(result)
                .message("Lấy danh sách thành công")
                .build();
    }

    // API thêm giao dịch mới
    @PostMapping
    public ApiResponse<TransactionResponseDTO> createTransaction(@Valid @RequestBody CreateTransactionRequestDTO request,
                                                                 @AuthenticationPrincipal Jwt jwt) {
        Long userId = Long.parseLong(jwt.getSubject());
        TransactionResponseDTO result = transactionFacade.addTransaction(request, userId);

        return ApiResponse.<TransactionResponseDTO>builder()
                .result(result)
                .message("Thêm giao dịch thành công")
                .build();
    }

    //API lấy danh sách symbol trong portfolio
//    @GetMapping("/portfolio")
//    public ApiResponse<List<PortfolioResponseDTO>> getPortfolio() {
//        List<PortfolioResponseDTO> result = transactionFacade.getPortfolios();
//
//        return ApiResponse.<List<PortfolioResponseDTO>>builder()
//                .result(result)
//                .message("Lấy danh sách portfolio thành công")
//                .build();
//    }
}
