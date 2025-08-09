package com.g47.cem.cemdevice.dto.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Request DTO for linking devices to customers
 */
@Data
public class LinkDevicesRequest {
    
    private Long contractId; // optional; required when linking from contract service
    
    @NotNull(message = "Customer ID is required")
    private Long customerId;
    
    @Valid
    @NotNull(message = "Device information is required")
    private List<DeviceInfo> devices;
    
    /**
     * Device information for linking
     */
    @Data
    public static class DeviceInfo {
        @NotNull(message = "Device ID is required")
        private Long deviceId;
        
        private Integer quantity = 1;
        
        private Integer warrantyMonths = 0;
    }
} 