package com.g47.cem.cemdevice.dto.request;

import com.g47.cem.cemdevice.enums.DeviceStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a new device
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateDeviceRequest {
    
    @NotBlank(message = "Device name is required")
    @Size(min = 1, max = 255, message = "Device name must be between 1 and 255 characters")
    private String name;
    
    @Size(max = 255, message = "Model must not exceed 255 characters")
    private String model;
    
    @Size(max = 255, message = "Serial number must not exceed 255 characters")
    private String serialNumber;
    
    private java.math.BigDecimal price;
    
    @Size(max = 50, message = "Unit must not exceed 50 characters")
    private String unit;
    
    @Builder.Default
    private DeviceStatus status = DeviceStatus.ACTIVE;
} 