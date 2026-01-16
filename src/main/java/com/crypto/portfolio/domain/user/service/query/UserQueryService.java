package com.crypto.portfolio.domain.user.service.query;

import com.crypto.portfolio.domain.user.entity.User;

public interface UserQueryService {
    User getByUserId(Long userId);
}
