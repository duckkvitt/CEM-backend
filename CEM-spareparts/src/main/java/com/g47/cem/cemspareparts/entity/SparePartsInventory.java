package com.g47.cem.cemspareparts.entity;

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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * SparePartsInventory entity representing spare parts inventory tracking
 */
@Entity
@Table(name = "spare_parts_inventory")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class SparePartsInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "spare_part_id", nullable = false, unique = true)
    private SparePart sparePart;

    @Column(name = "quantity_in_stock", nullable = false)
    @Builder.Default
    private Integer quantityInStock = 0;

    @Column(name = "minimum_stock_level")
    @Builder.Default
    private Integer minimumStockLevel = 0;

    @Column(name = "maximum_stock_level")
    @Builder.Default
    private Integer maximumStockLevel = 1000;

    @Column(name = "last_restocked_at")
    private LocalDateTime lastRestockedAt;

    @Column(name = "last_updated_by")
    private String lastUpdatedBy;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Helper methods
    public boolean isLowStock() {
        return quantityInStock <= minimumStockLevel;
    }

    public boolean isOverStock() {
        return quantityInStock >= maximumStockLevel;
    }

    public boolean isOutOfStock() {
        return quantityInStock <= 0;
    }

    public void addStock(Integer quantity, String updatedBy) {
        this.quantityInStock += quantity;
        this.lastRestockedAt = LocalDateTime.now();
        this.lastUpdatedBy = updatedBy;
    }

    public boolean removeStock(Integer quantity, String updatedBy) {
        if (this.quantityInStock >= quantity) {
            this.quantityInStock -= quantity;
            this.lastUpdatedBy = updatedBy;
            return true;
        }
        return false;
    }

    public void adjustStock(Integer newQuantity, String updatedBy) {
        this.quantityInStock = newQuantity;
        this.lastUpdatedBy = updatedBy;
    }

    /**
     * Custom equals method that only uses ID to avoid circular dependencies.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        SparePartsInventory inventory = (SparePartsInventory) obj;
        return id != null && id.equals(inventory.id);
    }

    /**
     * Custom hashCode method that only uses ID to avoid circular dependencies.
     */
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
