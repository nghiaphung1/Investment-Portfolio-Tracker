package com.crypto.portfolio.domain.user.mapper;

import com.crypto.portfolio.domain.user.dto.UserResponseDTO;
import com.crypto.portfolio.domain.user.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    // User Entity to UserResponseDTO
    UserResponseDTO toUserResponse(User user);
}
