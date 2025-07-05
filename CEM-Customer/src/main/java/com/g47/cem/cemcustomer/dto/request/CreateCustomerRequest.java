package com.g47.cem.cemcustomer.dto.request;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    
    @NotBlank(message = "Phone number is required")
    @Pattern(
        regexp = "^[+]?[0-9]{10,20}$",
        message = "Phone number must be between 10-20 digits and can start with +"
    )
    private String phone;
    
    @Size(max = 1000, message = "Address must not exceed 1000 characters")
    private String address;

    @NotBlank(message = "Company name is required")
    @Size(max = 255, message = "Company name must not exceed 255 characters")
    private String companyName;

    @NotBlank(message = "Company tax code is required")
    @Size(max = 50, message = "Company tax code must not exceed 50 characters")
    private String companyTaxCode;

    @NotBlank(message = "Company address is required")
    @Size(max = 1000, message = "Company address must not exceed 1000 characters")
    private String companyAddress;

    @NotBlank(message = "Legal representative is required")
    @Size(max = 255, message = "Legal representative must not exceed 255 characters")
    private String legalRepresentative;

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @NotBlank(message = "Identity number is required")
    @Size(max = 50, message = "Identity number must not exceed 50 characters")
    private String identityNumber;

    @NotNull(message = "Identity issue date is required")
    private LocalDate identityIssueDate;

    @NotBlank(message = "Identity issue place is required")
    @Size(max = 255, message = "Identity issue place must not exceed 255 characters")
    private String identityIssuePlace;

    @Size(max = 20, message = "Fax must not exceed 20 characters")
    private String fax;
    
    private List<String> tags;
    
    @Builder.Default
    private Boolean isHidden = false;
} 