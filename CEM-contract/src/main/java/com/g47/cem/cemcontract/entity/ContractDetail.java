package com.g47.cem.cemcontract.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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
 * ContractDetail entity representing individual line items in a contract
 */
@Entity
@Table(name = "contract_details")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ContractDetail {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;
    
    @Column(name = "work_code", nullable = false, length = 100)
    private String workCode; // Work/Service code
    
    @Column(name = "device_id")
    private Long deviceId; // Reference to device (optional)
    
    @Column(name = "service_name", nullable = false, length = 255)
    private String serviceName;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false)
    @Builder.Default
    private Integer quantity = 1;
    
    @Column(name = "unit_price", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal unitPrice = BigDecimal.ZERO;
    
    @Column(name = "total_price", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal totalPrice = BigDecimal.ZERO;
    
    @Column(name = "warranty_months")
    @Builder.Default
    private Integer warrantyMonths = 0; // Warranty period in months
    
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Helper methods
    
    /**
     * Calculate total price based on quantity and unit price
     */
    public void calculateTotalPrice() {
        if (unitPrice != null && quantity != null) {
            this.totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));
        } else {
            this.totalPrice = BigDecimal.ZERO;
        }
    }
    
    /**
     * Set quantity and recalculate total price
     */
    public void setQuantityAndRecalculate(Integer quantity) {
        this.quantity = quantity;
        calculateTotalPrice();
    }
    
    /**
     * Set unit price and recalculate total price
     */
    public void setUnitPriceAndRecalculate(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
        calculateTotalPrice();
    }
    
    /**
     * Check if this detail is associated with a device
     */
    public boolean hasDevice() {
        return deviceId != null;
    }
    
    /**
     * Check if warranty is applicable
     */
    public boolean hasWarranty() {
        return warrantyMonths != null && warrantyMonths > 0;
    }
} 