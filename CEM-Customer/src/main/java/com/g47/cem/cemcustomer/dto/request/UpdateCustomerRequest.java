package com.g47.cem.cemcustomer.dto.request;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating an existing customer. All fields are optional. If a
 * field is null it will be ignored and the current value kept.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateCustomerRequest {

    @Size(min = 1, max = 255, message = "Name must be between 1 and 255 characters")
    private String name;

    @Email(message = "Invalid email format")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;

    @Pattern(regexp = "^[+]?[0-9]{10,20}$", message = "Phone number must be between 10-20 digits and can start with +")
    private String phone;

    @Size(max = 1000, message = "Address must not exceed 1000 characters")
    private String address;

    @Size(max = 255, message = "Company name must not exceed 255 characters")
    private String companyName;

    @Size(max = 50, message = "Company tax code must not exceed 50 characters")
    private String companyTaxCode;

    @Size(max = 1000, message = "Company address must not exceed 1000 characters")
    private String companyAddress;

    @Size(max = 255, message = "Legal representative must not exceed 255 characters")
    private String legalRepresentative;

    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @Size(max = 50, message = "Identity number must not exceed 50 characters")
    private String identityNumber;

    private LocalDate identityIssueDate;

    @Size(max = 255, message = "Identity issue place must not exceed 255 characters")
    private String identityIssuePlace;

    @Size(max = 20, message = "Fax must not exceed 20 characters")
    private String fax;

    private List<String> tags;

    private Boolean isHidden;
} 