package com.g47.cem.cemcontract.dto.response.external;

import lombok.Data;

@Data
public class UserResponse {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private String phone;
    // Các trường khác có thể được thêm vào nếu cần
    // For now, we only need a subset of fields from the actual UserResponse
} 