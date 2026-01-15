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
    CryptoTransaction toEntity(TransactionRequestDTO request);

    TransactionResponseDTO toResponse(CryptoTransaction entity);

    List<TransactionResponseDTO> toResponseList(List<CryptoTransaction> entities);

}
