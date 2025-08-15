package com.g47.cem.cemdevice.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.g47.cem.cemdevice.enums.InventoryReferenceType;
import com.g47.cem.cemdevice.enums.InventoryTransactionType;

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
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DeviceInventoryTransaction entity representing device inventory transaction history
 */
@Entity
@Table(name = "device_inventory_transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class DeviceInventoryTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_number", nullable = false, unique = true)
    private String transactionNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

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

    public static DeviceInventoryTransaction createImportTransaction(
            Device device, Integer quantity, Integer beforeQuantity, 
            Long importRequestId, String reason, String createdBy, String transactionNumber) {
        return DeviceInventoryTransaction.builder()
                .transactionNumber(transactionNumber)
                .device(device)
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

    public static DeviceInventoryTransaction createExportTransaction(
            Device device, Integer quantity, Integer beforeQuantity, 
            Long contractId, String reason, String createdBy, String transactionNumber) {
        return DeviceInventoryTransaction.builder()
                .transactionNumber(transactionNumber)
                .device(device)
                .transactionType(InventoryTransactionType.EXPORT)
                .quantityChange(-quantity)
                .quantityBefore(beforeQuantity)
                .quantityAfter(beforeQuantity - quantity)
                .referenceType(InventoryReferenceType.CONTRACT)
                .referenceId(contractId)
                .transactionReason(reason)
                .createdBy(createdBy)
                .build();
    }

    public static DeviceInventoryTransaction createAdjustmentTransaction(
            Device device, Integer newQuantity, Integer beforeQuantity, 
            String reason, String createdBy, String transactionNumber) {
        return DeviceInventoryTransaction.builder()
                .transactionNumber(transactionNumber)
                .device(device)
                .transactionType(InventoryTransactionType.ADJUSTMENT)
                .quantityChange(newQuantity - beforeQuantity)
                .quantityBefore(beforeQuantity)
                .quantityAfter(newQuantity)
                .referenceType(InventoryReferenceType.ADJUSTMENT)
                .transactionReason(reason)
                .createdBy(createdBy)
                .build();
    }
}
