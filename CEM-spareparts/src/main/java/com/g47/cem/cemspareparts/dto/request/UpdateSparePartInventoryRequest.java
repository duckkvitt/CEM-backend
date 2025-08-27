package com.g47.cem.cemspareparts.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating SparePartInventory
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSparePartInventoryRequest {
    
    @Min(value = 0, message = "Quantity in stock must be non-negative")
    private Integer quantityInStock;
    
    @Min(value = 0, message = "Minimum stock level must be non-negative")
    private Integer minimumStockLevel;
    
    @Min(value = 1, message = "Maximum stock level must be positive")
    private Integer maximumStockLevel;
    
    @Min(value = 0, message = "Reorder point must be non-negative")
    private Integer reorderPoint;
    
    @Min(value = 0, message = "Unit cost must be non-negative")
    private BigDecimal unitCost;
    
    private String warehouseLocation;
    
    private String notes;
}


