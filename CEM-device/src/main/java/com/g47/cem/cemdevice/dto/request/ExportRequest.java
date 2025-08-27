package com.g47.cem.cemdevice.dto.request;

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
 * Request DTO for exporting inventory items
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExportRequest {
    
    @NotNull(message = "Item type is required")
    private InventoryItemType itemType;
    
    @NotNull(message = "Reference type is required")
    private String referenceType; // Task, Contract, etc.
    
    @NotNull(message = "Reference ID is required")
    private Long referenceId;
    
    private String referenceNumber; // Task number, contract number, etc.
    
    private String warehouseLocation;
    
    private String notes;
    
    @Valid
    @NotEmpty(message = "At least one item must be exported")
    private List<ExportItem> items;
    
    /**
     * Individual export item
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ExportItem {
        
        @NotNull(message = "Item ID is required")
        private Long itemId;
        
        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be positive")
        private Integer quantity;
        
        private String notes;
    }
}


