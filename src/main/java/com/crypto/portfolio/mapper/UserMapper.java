package com.crypto.portfolio.mapper;

import com.crypto.portfolio.dto.users.UserResponseDTO;
import com.crypto.portfolio.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    // User Entity to UserResponseDTO
    UserResponseDTO toUserResponse(User user);
}
