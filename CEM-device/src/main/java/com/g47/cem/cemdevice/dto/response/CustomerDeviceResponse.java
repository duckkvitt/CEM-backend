package com.g47.cem.cemdevice.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.g47.cem.cemdevice.enums.CustomerDeviceStatus;

import lombok.Data;

/**
 * Response DTO for CustomerDevice operations
 */
@Data
public class CustomerDeviceResponse {
    
    // CustomerDevice fields
    private Long id;
    private Long customerId;
    private LocalDate warrantyEnd;
    private CustomerDeviceStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Device fields
    private Long deviceId;
    private String deviceName;
    private String deviceModel;
    private String serialNumber;
    private BigDecimal devicePrice;
    private String deviceUnit;
    
    // Calculated fields
    private Boolean warrantyExpired;
    private Boolean warrantyExpiringSoon;
    
    // Helper methods
    public boolean isWarrantyExpired() {
        return warrantyExpired != null && warrantyExpired;
    }
    
    public boolean isWarrantyExpiringSoon() {
        return warrantyExpiringSoon != null && warrantyExpiringSoon;
    }
    
    public boolean isActive() {
        return status == CustomerDeviceStatus.ACTIVE;
    }
    
    public boolean isInactive() {
        return status == CustomerDeviceStatus.INACTIVE;
    }
    
    public boolean isError() {
        return status == CustomerDeviceStatus.ERROR;
    }
} 