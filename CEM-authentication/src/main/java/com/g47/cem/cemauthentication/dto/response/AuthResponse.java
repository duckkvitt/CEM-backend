package com.g47.cem.cemauthentication.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Authentication response DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {
    
    private String accessToken;
    private String refreshToken;
    
    @Builder.Default
    private String tokenType = "Bearer";
    
    private Long expiresIn;
    private UserResponse user;
    private LocalDateTime issuedAt;
    
    public static AuthResponse of(String accessToken, String refreshToken, Long expiresIn, UserResponse user) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .user(user)
                .issuedAt(LocalDateTime.now())
                .build();
    }
} 