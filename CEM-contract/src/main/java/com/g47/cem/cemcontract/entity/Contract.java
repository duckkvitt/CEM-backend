package com.g47.cem.cemcontract.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.g47.cem.cemcontract.enums.ContractStatus;

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
 * Contract entity representing contract information
 */
@Entity
@Table(name = "contracts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Contract {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "customer_id", nullable = false)
    private Long customerId;
    
    @Column(name = "staff_id", nullable = false)
    private Long staffId; // userId from authentication service
    
    @Column(name = "contract_number", unique = true, nullable = false, length = 50)
    private String contractNumber; // Auto-generated contract number
    
    @Column(nullable = false, length = 255)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Builder.Default
    private ContractStatus status = ContractStatus.DRAFT;
    
    @Column(name = "file_path", length = 500)
    private String filePath; // Path to contract document
    
    @Column(name = "digital_signed", nullable = false)
    @Builder.Default
    private Boolean digitalSigned = false;
    
    @Column(name = "paper_confirmed", nullable = false)
    @Builder.Default
    private Boolean paperConfirmed = false;
    
    @Column(name = "signed_at")
    private LocalDateTime signedAt; // When the contract was signed
    
    @Column(name = "signed_by", length = 255)
    private String signedBy; // Who signed the contract
    
    @Column(name = "total_value", precision = 15, scale = 2)
    private BigDecimal totalValue; // Total contract value
    
    @Column(name = "start_date")
    private LocalDate startDate; // Contract start date
    
    @Column(name = "end_date")
    private LocalDate endDate; // Contract end date
    
    // Điều 2: Thanh toán
    @Column(name = "payment_method", length = 255)
    private String paymentMethod; // Hình thức thanh toán
    
    @Column(name = "payment_term", length = 500)
    private String paymentTerm; // Thời hạn thanh toán
    
    @Column(name = "bank_account", length = 500)
    private String bankAccount; // Tài khoản ngân hàng
    
    // Điều 3: Thời gian, địa điểm, phương thức giao hàng (now managed by ContractDeliverySchedule table)
    
    // Điều 5: Bảo hành và hướng dẫn sử dụng hàng hóa
    @Column(name = "warranty_product", length = 500)
    private String warrantyProduct; // Loại hàng bảo hành
    
    @Column(name = "warranty_period_months")
    private Integer warrantyPeriodMonths; // Thời gian bảo hành (tháng)

    @Column(name = "is_hidden", nullable = false)
    @Builder.Default
    private Boolean isHidden = false;
    
    @Column(name = "created_by", nullable = false)
    private String createdBy;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Relationships
    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<ContractDetail> contractDetails;
    
    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<ContractSignature> signatures;
    
    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<ContractHistory> history;
    
    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<ContractDeliverySchedule> deliverySchedules;
    
    // Helper methods
    public boolean isVisible() {
        return !isHidden;
    }
    
    public void hide() {
        this.isHidden = true;
    }
    
    public void show() {
        this.isHidden = false;
    }
    
    public boolean isFullySigned() {
        return status == ContractStatus.ACTIVE;
    }
    
    public boolean isActive() {
        return status == ContractStatus.ACTIVE;
    }
    
    /* This method is too simplistic for the new workflow.
     * Signing logic will be handled in the service layer by creating
     * ContractSignature entities and updating status based on business rules.
    public void sign(String signerName, ContractStatus newStatus) {
        this.status = newStatus;
        this.signedAt = LocalDateTime.now();
        this.signedBy = signerName;
        
        if (newStatus == ContractStatus.ACTIVE) {
            this.digitalSigned = true;
        }
    }
    */
    
    /**
     * Calculate total value from contract details if not set manually
     */
    public BigDecimal calculateTotalValue() {
        if (contractDetails == null || contractDetails.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        return contractDetails.stream()
                .map(detail -> detail.getTotalPrice())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * Update total value from contract details
     */
    public void updateTotalValue() {
        this.totalValue = calculateTotalValue();
    }
} 