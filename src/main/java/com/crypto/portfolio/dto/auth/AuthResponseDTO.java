package com.crypto.portfolio.dto.auth;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpHeaders;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponseDTO {
    private String accessToken;
    @JsonIgnore
    private String refreshToken;
    private Long id;
    private String email;
    private String fullName;
    private String avatar;
}
