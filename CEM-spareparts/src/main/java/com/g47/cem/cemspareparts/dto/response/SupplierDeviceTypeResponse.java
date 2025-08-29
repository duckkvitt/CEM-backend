package com.g47.cem.cemspareparts.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class SupplierDeviceTypeResponse {
    
    private Long id;
    private Long supplierId;
    private String supplierCompanyName;
    private String deviceType;
    private String deviceModel;
    private BigDecimal unitPrice;
    private Integer minimumOrderQuantity;
    private Integer leadTimeDays;
    private String notes;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Computed fields
    public String getFullDeviceDescription() {
        if (deviceModel != null && !deviceModel.trim().isEmpty()) {
            return deviceType + " - " + deviceModel;
        }
        return deviceType;
    }
    
    public boolean isActiveSupply() {
        return Boolean.TRUE.equals(isActive);
    }
}


