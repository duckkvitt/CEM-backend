package com.g47.cem.cemdevice.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.g47.cem.cemdevice.enums.CustomerDeviceStatus;

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
 * CustomerDevice entity representing customer-owned devices
 */
@Entity
@Table(name = "customer_devices")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class CustomerDevice {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "customer_id", nullable = false)
    private Long customerId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;
    
    @Column(name = "purchase_date")
    private LocalDate purchaseDate;
    
    @Column(name = "warranty_start")
    private LocalDate warrantyStart;
    
    @Column(name = "warranty_end")
    private LocalDate warrantyEnd;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Builder.Default
    private CustomerDeviceStatus status = CustomerDeviceStatus.ACTIVE;
    
    @Column(columnDefinition = "TEXT")
    private String note;
    
    @Column(name = "created_by")
    private String createdBy;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Helper methods
    public boolean isActive() {
        return status == CustomerDeviceStatus.ACTIVE;
    }
    
    public boolean isUnderWarranty() {
        return warrantyEnd != null && warrantyEnd.isAfter(LocalDate.now());
    }
    
    public void activate() {
        this.status = CustomerDeviceStatus.ACTIVE;
    }
    
    public void setError() {
        this.status = CustomerDeviceStatus.ERROR;
    }
    
    public void setWarranty() {
        this.status = CustomerDeviceStatus.WARRANTY;
    }
    
    public void setExpired() {
        this.status = CustomerDeviceStatus.EXPIRED;
    }
} 