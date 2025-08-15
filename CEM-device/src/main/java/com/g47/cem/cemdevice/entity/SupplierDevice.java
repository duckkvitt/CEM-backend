package com.g47.cem.cemdevice.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
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
 * SupplierDevice entity representing supplier-device relationships
 */
@Entity
@Table(name = "supplier_devices")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class SupplierDevice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "supplier_id", nullable = false)
    private Long supplierId; // Reference to supplier in spare parts service

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @Column(name = "is_primary_supplier")
    @Builder.Default
    private Boolean isPrimarySupplier = false;

    @Column(name = "unit_price", precision = 15, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "minimum_order_quantity")
    @Builder.Default
    private Integer minimumOrderQuantity = 1;

    @Column(name = "lead_time_days")
    @Builder.Default
    private Integer leadTimeDays = 0;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Helper methods
    public boolean isPrimary() {
        return Boolean.TRUE.equals(isPrimarySupplier);
    }

    public void setPrimary() {
        this.isPrimarySupplier = true;
    }

    public void unsetPrimary() {
        this.isPrimarySupplier = false;
    }

    public boolean hasMinimumOrderQuantity(Integer orderQuantity) {
        return orderQuantity >= minimumOrderQuantity;
    }

    public Integer getEstimatedDeliveryDays() {
        return leadTimeDays != null ? leadTimeDays : 0;
    }
}
