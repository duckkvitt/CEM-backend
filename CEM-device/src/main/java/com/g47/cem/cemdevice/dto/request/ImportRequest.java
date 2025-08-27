package com.g47.cem.cemdevice.dto.request;

import java.math.BigDecimal;
import java.util.List;

import com.g47.cem.cemdevice.enums.InventoryItemType;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for importing inventory items
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ImportRequest {
    
    @NotNull(message = "Item type is required")
    private InventoryItemType itemType;
    
    @NotNull(message = "Supplier ID is required")
    private Long supplierId;
    
    private String referenceNumber; // PO number, invoice number, etc.
    
    private String warehouseLocation;
    
    private String notes;
    
    @Valid
    @NotEmpty(message = "At least one item must be imported")
    private List<ImportItem> items;
    
    /**
     * Individual import item
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ImportItem {
        
        @NotNull(message = "Item ID is required")
        private Long itemId;
        
        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be positive")
        private Integer quantity;
        
        @Positive(message = "Unit price must be positive")
        private BigDecimal unitPrice;
        
        private BigDecimal unitCost;
        
        private Integer minimumStockLevel;
        
        private Integer maximumStockLevel;
        
        private Integer reorderPoint;
        
        private String notes;
    }
}
