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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * SparePartInventory entity representing spare part stock in warehouse
 */
@Entity
@Table(name = "spare_part_inventory")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class SparePartInventory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "spare_part_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private SparePart sparePart;
    
    @Column(name = "quantity_in_stock", nullable = false)
    @Builder.Default
    private Integer quantityInStock = 0;
    
    @Column(name = "minimum_stock_level")
    @Builder.Default
    private Integer minimumStockLevel = 0;
    
    @Column(name = "maximum_stock_level")
    private Integer maximumStockLevel;
    
    @Column(name = "reorder_point")
    private Integer reorderPoint;
    
    @Column(name = "unit_cost", precision = 15, scale = 2)
    private java.math.BigDecimal unitCost;
    
    @Column(name = "warehouse_location", length = 100)
    private String warehouseLocation;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @Column(name = "created_by")
    private String createdBy;
    
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
    
    public boolean needsReorder() {
        return reorderPoint != null && quantityInStock <= reorderPoint;
    }
    
    public boolean isOutOfStock() {
        return quantityInStock <= 0;
    }
    
    public void addStock(Integer quantity) {
        if (quantity > 0) {
            this.quantityInStock += quantity;
        }
    }
    
    public void removeStock(Integer quantity) {
        if (quantity > 0 && this.quantityInStock >= quantity) {
            this.quantityInStock -= quantity;
        }
    }
}


