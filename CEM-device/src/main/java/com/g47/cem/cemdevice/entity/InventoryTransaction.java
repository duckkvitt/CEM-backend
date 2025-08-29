package com.g47.cem.cemdevice.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.g47.cem.cemdevice.enums.InventoryTransactionType;
import com.g47.cem.cemdevice.enums.InventoryItemType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * InventoryTransaction entity representing all inventory movements
 */
@Entity
@Table(name = "inventory_transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class InventoryTransaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "transaction_number", unique = true, nullable = false, length = 50)
    private String transactionNumber;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 50)
    private InventoryTransactionType transactionType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false, length = 50)
    private InventoryItemType itemType;
    
    @Column(name = "item_id", nullable = false)
    private Long itemId;
    
    @Column(name = "item_name", nullable = false, length = 255)
    private String itemName;
    
    @Column(name = "quantity", nullable = false)
    private Integer quantity;
    
    @Column(name = "unit_price", precision = 15, scale = 2)
    private BigDecimal unitPrice;
    
    @Column(name = "total_amount", precision = 15, scale = 2)
    private BigDecimal totalAmount;
    
    @Column(name = "supplier_id")
    private Long supplierId;
    
    @Column(name = "supplier_name", length = 255)
    private String supplierName;
    
    @Column(name = "reference_number", length = 100)
    private String referenceNumber; // PO number, invoice number, etc.
    
    @Column(name = "reference_type", length = 50)
    private String referenceType; // PO, Invoice, Contract, Task, etc.
    
    @Column(name = "reference_id")
    private Long referenceId;
    
    @Column(name = "transaction_reason", columnDefinition = "TEXT")
    private String transactionReason;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @Column(name = "warehouse_location", length = 100)
    private String warehouseLocation;
    
    @Column(name = "created_by", nullable = false, length = 255)
    private String createdBy;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Helper methods
    public boolean isImport() {
        return transactionType == InventoryTransactionType.IMPORT;
    }
    
    public boolean isExport() {
        return transactionType == InventoryTransactionType.EXPORT;
    }
    
    public boolean isAdjustment() {
        return transactionType == InventoryTransactionType.ADJUSTMENT;
    }
    
    public boolean isDevice() {
        return itemType == InventoryItemType.DEVICE;
    }
    
    public boolean isSparePart() {
        return itemType == InventoryItemType.SPARE_PART;
    }
}


