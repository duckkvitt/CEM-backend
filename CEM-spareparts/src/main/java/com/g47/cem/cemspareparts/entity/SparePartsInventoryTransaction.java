package com.g47.cem.cemspareparts.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.g47.cem.cemspareparts.enums.InventoryTransactionType;
import com.g47.cem.cemspareparts.enums.InventoryReferenceType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * SparePartsInventoryTransaction entity representing spare parts inventory transaction history
 */
@Entity
@Table(name = "spare_parts_inventory_transactions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class SparePartsInventoryTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_number", nullable = false, unique = true)
    private String transactionNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "spare_part_id", nullable = false)
    private SparePart sparePart;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private InventoryTransactionType transactionType;

    @Column(name = "quantity_change", nullable = false)
    private Integer quantityChange;

    @Column(name = "quantity_before", nullable = false)
    private Integer quantityBefore;

    @Column(name = "quantity_after", nullable = false)
    private Integer quantityAfter;

    @Enumerated(EnumType.STRING)
    @Column(name = "reference_type")
    private InventoryReferenceType referenceType;

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(name = "transaction_reason", columnDefinition = "TEXT")
    private String transactionReason;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

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

    public boolean isTransfer() {
        return transactionType == InventoryTransactionType.TRANSFER;
    }

    public static SparePartsInventoryTransaction createImportTransaction(
            SparePart sparePart, Integer quantity, Integer beforeQuantity, 
            Long importRequestId, String reason, String createdBy, String transactionNumber) {
        return SparePartsInventoryTransaction.builder()
                .transactionNumber(transactionNumber)
                .sparePart(sparePart)
                .transactionType(InventoryTransactionType.IMPORT)
                .quantityChange(quantity)
                .quantityBefore(beforeQuantity)
                .quantityAfter(beforeQuantity + quantity)
                .referenceType(InventoryReferenceType.IMPORT_REQUEST)
                .referenceId(importRequestId)
                .transactionReason(reason)
                .createdBy(createdBy)
                .build();
    }

    public static SparePartsInventoryTransaction createExportTransaction(
            SparePart sparePart, Integer quantity, Integer beforeQuantity, 
            Long exportRequestId, String reason, String createdBy, String transactionNumber) {
        return SparePartsInventoryTransaction.builder()
                .transactionNumber(transactionNumber)
                .sparePart(sparePart)
                .transactionType(InventoryTransactionType.EXPORT)
                .quantityChange(-quantity)
                .quantityBefore(beforeQuantity)
                .quantityAfter(beforeQuantity - quantity)
                .referenceType(InventoryReferenceType.EXPORT_REQUEST)
                .referenceId(exportRequestId)
                .transactionReason(reason)
                .createdBy(createdBy)
                .build();
    }

    public static SparePartsInventoryTransaction createAdjustmentTransaction(
            SparePart sparePart, Integer newQuantity, Integer beforeQuantity, 
            String reason, String createdBy, String transactionNumber) {
        return SparePartsInventoryTransaction.builder()
                .transactionNumber(transactionNumber)
                .sparePart(sparePart)
                .transactionType(InventoryTransactionType.ADJUSTMENT)
                .quantityChange(newQuantity - beforeQuantity)
                .quantityBefore(beforeQuantity)
                .quantityAfter(newQuantity)
                .referenceType(InventoryReferenceType.ADJUSTMENT)
                .transactionReason(reason)
                .createdBy(createdBy)
                .build();
    }

    /**
     * Custom equals method that only uses ID to avoid circular dependencies.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        SparePartsInventoryTransaction transaction = (SparePartsInventoryTransaction) obj;
        return id != null && id.equals(transaction.id);
    }

    /**
     * Custom hashCode method that only uses ID to avoid circular dependencies.
     */
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
