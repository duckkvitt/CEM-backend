package com.g47.cem.cemdevice.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.g47.cem.cemdevice.enums.DeviceStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Device entity representing device information
 */
@Entity
@Table(name = "devices")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Device {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 255)
    private String name;
    
    @Column(length = 255)
    private String model;
    
    @Column(name = "serial_number", unique = true, length = 255)
    private String serialNumber;
    
    @Column(name = "customer_id")
    private Long customerId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Builder.Default
    private DeviceStatus status = DeviceStatus.ACTIVE;
    
    @Column(name = "price", precision = 15, scale = 2)
    private java.math.BigDecimal price;
    
    @Column(name = "unit", length = 50)
    private String unit;
    
    @Column(name = "warranty_expiry")
    private LocalDate warrantyExpiry;
    
    @Column(name = "created_by")
    private String createdBy;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Relationships
    @OneToMany(mappedBy = "device", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<DeviceNote> deviceNotes;
    
    @OneToMany(mappedBy = "device", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<CustomerDevice> customerDevices;
    
    // Helper methods
    public boolean isActive() {
        return status == DeviceStatus.ACTIVE;
    }
    
    public void activate() {
        this.status = DeviceStatus.ACTIVE;
    }
    
    public void deactivate() {
        this.status = DeviceStatus.INACTIVE;
    }
    
    public void setMaintenance() {
        this.status = DeviceStatus.MAINTENANCE;
    }
    
    public void setBroken() {
        this.status = DeviceStatus.BROKEN;
    }
} 