package com.g47.cem.cemspareparts.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.g47.cem.cemspareparts.entity.SparePartInventory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for SparePartInventory
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SparePartInventoryResponse {
    
    private Long id;
    private Long sparePartId;
    private String partName;
    private String partCode;
    private String description;
    private String unitOfMeasurement;
    private String status;
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
    
    // Spare part details
    private SparePartResponse sparePart;
    
    public static SparePartInventoryResponse fromEntity(SparePartInventory entity) {
        if (entity == null) {
            return null;
        }
        
        return SparePartInventoryResponse.builder()
                .id(entity.getId())
                .sparePartId(entity.getSparePart().getId())
                .partName(entity.getSparePart().getPartName())
                .partCode(entity.getSparePart().getPartCode())
                .description(entity.getSparePart().getDescription())
                .unitOfMeasurement(entity.getSparePart().getUnitOfMeasurement())
                .status(entity.getSparePart().getStatus().name())
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


