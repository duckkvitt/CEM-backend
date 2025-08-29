package com.g47.cem.cemspareparts.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class CreateSupplierDeviceTypeRequest {
    
    @NotNull(message = "Supplier ID is required")
    private Long supplierId;
    
    @NotBlank(message = "Device type is required")
    private String deviceType;
    
    private String deviceModel;
    
    @PositiveOrZero(message = "Unit price must be positive or zero")
    private BigDecimal unitPrice;
    
    @Positive(message = "Minimum order quantity must be positive")
    private Integer minimumOrderQuantity = 1;
    
    @PositiveOrZero(message = "Lead time days must be positive or zero")
    private Integer leadTimeDays = 0;
    
    private String notes;
}


