package com.crypto.portfolio.mapper;

import com.crypto.portfolio.dto.transactions.TransactionRequestDTO;
import com.crypto.portfolio.dto.transactions.TransactionResponseDTO;
import com.crypto.portfolio.dto.users.UserResponseDTO;
import com.crypto.portfolio.entity.CryptoTransaction;
import com.crypto.portfolio.entity.User;
import org.mapstruct.Mapper;

import java.util.List;

// componentModel = "spring" giúp Spring quản lý Mapper này như một Bean (@Component)
@Mapper(componentModel = "spring")
public interface TransactionMapper {
    // Chiều 1: DTO (Request) -> Entity (Dùng khi thêm mới)
    CryptoTransaction toEntity(TransactionRequestDTO request);

    // Chiều 2: Entity -> DTO (Response) (Dùng khi hiển thị ra)
    TransactionResponseDTO toResponse(CryptoTransaction entity);

    // Chiều 2 mở rộng: List<Entity> -> List<DTO>
    // MapStruct tự động chạy vòng lặp for cho bạn luôn!
    List<TransactionResponseDTO> toResponseList(List<CryptoTransaction> entities);

}
