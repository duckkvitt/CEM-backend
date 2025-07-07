package com.g47.cem.cemcontract.entity;

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
 * Contract Delivery Schedule entity for "Điều 3. Thời gian, địa điểm, phương thức giao hàng"
 * Represents the delivery schedule table in the contract
 */
@Entity
@Table(name = "contract_delivery_schedules")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ContractDeliverySchedule {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Reference to the contract
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;
    
    /**
     * STT - Sequential number in the table
     */
    @Column(name = "sequence_number", nullable = false)
    private Integer sequenceNumber;
    
    /**
     * Tên hàng hóa
     */
    @Column(name = "item_name", nullable = false, length = 500)
    private String itemName;
    
    /**
     * Đơn vị
     */
    @Column(name = "unit", nullable = false, length = 50)
    private String unit;
    
    /**
     * Số lượng
     */
    @Column(name = "quantity", nullable = false)
    @Builder.Default
    private Integer quantity = 1;
    
    /**
     * Thời gian giao hàng
     */
    @Column(name = "delivery_time", length = 255)
    private String deliveryTime;
    
    /**
     * Địa điểm giao hàng
     */
    @Column(name = "delivery_location", columnDefinition = "TEXT")
    private String deliveryLocation;
    
    /**
     * Ghi chú
     */
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
} 