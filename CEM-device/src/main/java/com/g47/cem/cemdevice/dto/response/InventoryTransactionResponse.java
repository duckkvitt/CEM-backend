package com.g47.cem.cemdevice.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.g47.cem.cemdevice.entity.InventoryTransaction;
import com.g47.cem.cemdevice.enums.InventoryItemType;
import com.g47.cem.cemdevice.enums.InventoryTransactionType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for InventoryTransaction
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InventoryTransactionResponse {
    
    private Long id;
    private String transactionNumber;
    private String transactionType;
    private String itemType;
    private Long itemId;
    private String itemName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalAmount;
    private Long supplierId;
    private String supplierName;
    private String referenceNumber;
    private String referenceType;
    private Long referenceId;
    private String transactionReason;
    private String notes;
    private String warehouseLocation;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Computed fields
    private Boolean isImport;
    private Boolean isExport;
    private Boolean isAdjustment;
    private Boolean isDevice;
    private Boolean isSparePart;
    
    public static InventoryTransactionResponse fromEntity(InventoryTransaction entity) {
        if (entity == null) {
            return null;
        }
        
        return InventoryTransactionResponse.builder()
                .id(entity.getId())
                .transactionNumber(entity.getTransactionNumber())
                .transactionType(entity.getTransactionType().name())
                .itemType(entity.getItemType().name())
                .itemId(entity.getItemId())
                .itemName(entity.getItemName())
                .quantity(entity.getQuantity())
                .unitPrice(entity.getUnitPrice())
                .totalAmount(entity.getTotalAmount())
                .supplierId(entity.getSupplierId())
                .supplierName(entity.getSupplierName())
                .referenceNumber(entity.getReferenceNumber())
                .referenceType(entity.getReferenceType())
                .referenceId(entity.getReferenceId())
                .transactionReason(entity.getTransactionReason())
                .notes(entity.getNotes())
                .warehouseLocation(entity.getWarehouseLocation())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .isImport(entity.isImport())
                .isExport(entity.isExport())
                .isAdjustment(entity.isAdjustment())
                .isDevice(entity.isDevice())
                .isSparePart(entity.isSparePart())
                .build();
    }
}


