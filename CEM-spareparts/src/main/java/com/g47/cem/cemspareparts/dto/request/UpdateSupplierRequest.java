package com.g47.cem.cemspareparts.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

@Data
public class UpdateSupplierRequest {

    @NotBlank(message = "Company name is required")
    @Size(max = 255, message = "Company name must be less than 255 characters")
    private String companyName;

    @NotBlank(message = "Contact person is required")
    @Size(max = 255, message = "Contact person must be less than 255 characters")
    private String contactPerson;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must be less than 255 characters")
    private String email;

    @NotBlank(message = "Phone is required")
    @Size(max = 20, message = "Phone must be less than 20 characters")
    private String phone;

    @Size(max = 20, message = "Fax must be less than 20 characters")
    private String fax;

    @NotBlank(message = "Address is required")
    private String address;

    @Size(max = 50, message = "Tax code must be less than 50 characters")
    private String taxCode;

    @Size(max = 100, message = "Business license must be less than 100 characters")
    private String businessLicense;

    @Size(max = 255, message = "Website must be less than 255 characters")
    private String website;

    private String description;

    private Set<Long> sparePartIds;
}