package com.g47.cem.cemcustomer.dto.request;

import java.util.List;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a new customer
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateCustomerRequest {
    
    @NotBlank(message = "Name is required")
    @Size(min = 1, max = 255, message = "Name must be between 1 and 255 characters")
    private String name;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;
    
    @Pattern(
        regexp = "^[+]?[0-9]{10,20}$",
        message = "Phone number must be between 10-20 digits and can start with +"
    )
    private String phone;
    
    @Size(max = 1000, message = "Address must not exceed 1000 characters")
    private String address;
    
    private List<String> tags;
    
    @Builder.Default
    private Boolean isHidden = false;
} 