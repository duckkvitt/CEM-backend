package com.g47.cem.cemspareparts.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class UpdateSupplierDeviceTypeRequest {
    
    @NotBlank(message = "Device type is required")
    private String deviceType;
    
    private String deviceModel;
    
    @PositiveOrZero(message = "Unit price must be positive or zero")
    private BigDecimal unitPrice;
    
    @Positive(message = "Minimum order quantity must be positive")
    private Integer minimumOrderQuantity;
    
    @PositiveOrZero(message = "Lead time days must be positive or zero")
    private Integer leadTimeDays;
    
    private String notes;
    
    private Boolean isActive;
}


