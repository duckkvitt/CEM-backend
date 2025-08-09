package com.g47.cem.cemspareparts.dto.response;

import com.g47.cem.cemspareparts.enums.SupplierStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
public class SupplierResponse {
    private Long id;
    private String companyName;
    private String contactPerson;
    private String email;
    private String phone;
    private String fax;
    private String address;
    private String taxCode;
    private String businessLicense;
    private String website;
    private String description;
    private Set<SparePartResponse> spareParts;
    private SupplierStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}