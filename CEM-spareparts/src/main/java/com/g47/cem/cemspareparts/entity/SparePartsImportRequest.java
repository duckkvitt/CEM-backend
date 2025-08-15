package com.g47.cem.cemspareparts.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.g47.cem.cemspareparts.enums.ImportRequestStatus;
import com.g47.cem.cemspareparts.enums.ApprovalStatus;

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
 * SparePartsImportRequest entity representing spare parts import requests
 */
@Entity
@Table(name = "spare_parts_import_requests")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class SparePartsImportRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_number", nullable = false, unique = true)
    private String requestNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "spare_part_id", nullable = false)
    private SparePart sparePart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @Column(name = "requested_quantity", nullable = false)
    private Integer requestedQuantity;

    @Column(name = "unit_price", precision = 15, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "total_amount", precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_status", nullable = false)
    @Builder.Default
    private ImportRequestStatus requestStatus = ImportRequestStatus.PENDING;

    @Column(name = "request_reason", columnDefinition = "TEXT")
    private String requestReason;

    @Column(name = "requested_by", nullable = false)
    private String requestedBy;

    @Column(name = "requested_at", nullable = false)
    @Builder.Default
    private LocalDateTime requestedAt = LocalDateTime.now();

    @Column(name = "reviewed_by")
    private String reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status")
    private ApprovalStatus approvalStatus;

    @Column(name = "approval_reason", columnDefinition = "TEXT")
    private String approvalReason;

    @Column(name = "expected_delivery_date")
    private LocalDate expectedDeliveryDate;

    @Column(name = "actual_delivery_date")
    private LocalDate actualDeliveryDate;

    @Column(name = "invoice_number")
    private String invoiceNumber;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Helper methods
    public boolean isPending() {
        return requestStatus == ImportRequestStatus.PENDING;
    }

    public boolean isApproved() {
        return approvalStatus == ApprovalStatus.APPROVED;
    }

    public boolean isRejected() {
        return approvalStatus == ApprovalStatus.REJECTED;
    }

    public boolean isCompleted() {
        return requestStatus == ImportRequestStatus.COMPLETED;
    }

    public void approve(String reviewedBy, String reason) {
        this.approvalStatus = ApprovalStatus.APPROVED;
        this.requestStatus = ImportRequestStatus.APPROVED;
        this.reviewedBy = reviewedBy;
        this.reviewedAt = LocalDateTime.now();
        this.approvalReason = reason;
    }

    public void reject(String reviewedBy, String reason) {
        this.approvalStatus = ApprovalStatus.REJECTED;
        this.requestStatus = ImportRequestStatus.REJECTED;
        this.reviewedBy = reviewedBy;
        this.reviewedAt = LocalDateTime.now();
        this.approvalReason = reason;
    }

    public void complete(LocalDate actualDeliveryDate, String invoiceNumber) {
        this.requestStatus = ImportRequestStatus.COMPLETED;
        this.actualDeliveryDate = actualDeliveryDate;
        this.invoiceNumber = invoiceNumber;
    }

    public void cancel() {
        this.requestStatus = ImportRequestStatus.CANCELLED;
    }

    public BigDecimal calculateTotalAmount() {
        if (unitPrice != null && requestedQuantity != null) {
            return unitPrice.multiply(BigDecimal.valueOf(requestedQuantity));
        }
        return BigDecimal.ZERO;
    }

    /**
     * Custom equals method that only uses ID to avoid circular dependencies.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        SparePartsImportRequest request = (SparePartsImportRequest) obj;
        return id != null && id.equals(request.id);
    }

    /**
     * Custom hashCode method that only uses ID to avoid circular dependencies.
     */
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
