package com.g47.cem.cemdevice.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.g47.cem.cemdevice.entity.DeviceInventory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for DeviceInventory
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeviceInventoryResponse {
    
    private Long id;
    private Long deviceId;
    private String deviceName;
    private String deviceModel;
    private String deviceSerialNumber;
    private String deviceStatus;
    private Integer quantityInStock;
    private Integer minimumStockLevel;
    private Integer maximumStockLevel;
    private Integer reorderPoint;
    private BigDecimal unitCost;
    private String warehouseLocation;
    private String notes;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Computed fields
    private Boolean isLowStock;
    private Boolean needsReorder;
    private Boolean isOutOfStock;
    
    // Device details
    private DeviceResponse device;
    
    public static DeviceInventoryResponse fromEntity(DeviceInventory entity) {
        if (entity == null) {
            return null;
        }
        
        return DeviceInventoryResponse.builder()
                .id(entity.getId())
                .deviceId(entity.getDevice().getId())
                .deviceName(entity.getDevice().getName())
                .deviceModel(entity.getDevice().getModel())
                .deviceSerialNumber(entity.getDevice().getSerialNumber())
                .deviceStatus(entity.getDevice().getStatus().name())
                .quantityInStock(entity.getQuantityInStock())
                .minimumStockLevel(entity.getMinimumStockLevel())
                .maximumStockLevel(entity.getMaximumStockLevel())
                .reorderPoint(entity.getReorderPoint())
                .unitCost(entity.getUnitCost())
                .warehouseLocation(entity.getWarehouseLocation())
                .notes(entity.getNotes())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .isLowStock(entity.isLowStock())
                .needsReorder(entity.needsReorder())
                .isOutOfStock(entity.isOutOfStock())
                .build();
    }
}


