package com.crypto.portfolio.domain.transaction.mapper;

import com.crypto.portfolio.domain.transaction.dto.TransactionRequestDTO;
import com.crypto.portfolio.domain.transaction.dto.TransactionResponseDTO;
import com.crypto.portfolio.domain.transaction.entity.CryptoTransaction;
import org.mapstruct.Mapper;

import java.util.List;

// componentModel = "spring" giúp Spring quản lý Mapper này như một Bean (@Component)
@Mapper(componentModel = "spring")
public interface TransactionMapper {
    CryptoTransaction toEntity(TransactionRequestDTO request);

    TransactionResponseDTO toResponse(CryptoTransaction entity);

    List<TransactionResponseDTO> toResponseList(List<CryptoTransaction> entities);

}
