package com.crypto.portfolio.domain.transaction.mapper;

import com.crypto.portfolio.domain.transaction.dto.CreateTransactionRequestDTO;
import com.crypto.portfolio.domain.transaction.dto.TransactionResponseDTO;
import com.crypto.portfolio.domain.transaction.entity.CryptoTransaction;
import com.crypto.portfolio.domain.transaction.model.CreateTransactionCommand;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.Instant;
import java.util.List;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    // MapStruct tự động gộp 2 nguồn tham số vào 1 đích
    @Mapping(target = "userId", source = "userId") // Lấy userId từ tham số hàm
    @Mapping(target = "transactionDate", source = "dto.transactionDate", qualifiedByName = "handleDate") // Xử lý logic ngày
    @Mapping(target = "symbol", expression = "java(dto.getSymbol().toUpperCase())") // Chuẩn hóa Symbol thành chữ hoa ngay lập tức
    CreateTransactionCommand toCommand(Long userId, CreateTransactionRequestDTO dto);

    // Logic xử lý: Nếu Date null -> Lấy giờ hiện tại (Instant.now)
    // Nếu có Date -> Giữ nguyên
    @Named("handleDate")
    default Instant handleDate(Instant date) {
        return date != null ? date : Instant.now();
    }

    // Map ngược lại để trả về cho Client
    // Lưu ý: Nếu Entity có relation với User, ta map ID ra ngoài
    TransactionResponseDTO toResponse(CryptoTransaction entity);

    List<TransactionResponseDTO> toResponseList(List<CryptoTransaction> entities);



}
