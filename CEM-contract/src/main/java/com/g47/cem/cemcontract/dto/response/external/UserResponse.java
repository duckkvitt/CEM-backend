package com.g47.cem.cemcontract.dto.response.external;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserResponse {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String role;
    private String accountStatus;
    private LocalDateTime createdAt;
} 