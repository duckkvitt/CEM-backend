package com.g47.cem.cemcontract.dto.request.external;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private Long roleId;
    private Boolean emailVerified = false;
} 