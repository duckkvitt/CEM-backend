package com.g47.cem.cemspareparts.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.g47.cem.cemspareparts.enums.ExportRequestStatus;
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
 * SparePartsExportRequest entity representing spare parts export/usage requests from technicians
 */
@Entity
@Table(name = "spare_parts_export_requests")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class SparePartsExportRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_number", nullable = false, unique = true)
    private String requestNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "spare_part_id", nullable = false)
    private SparePart sparePart;

    @Column(name = "task_id")
    private Long taskId; // Reference to task in device service

    @Column(name = "requested_quantity", nullable = false)
    private Integer requestedQuantity;

    @Column(name = "request_reason", columnDefinition = "TEXT", nullable = false)
    private String requestReason;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_status", nullable = false)
    @Builder.Default
    private ExportRequestStatus requestStatus = ExportRequestStatus.PENDING;

    @Column(name = "requested_by", nullable = false)
    private String requestedBy; // technician

    @Column(name = "requested_at", nullable = false)
    @Builder.Default
    private LocalDateTime requestedAt = LocalDateTime.now();

    @Column(name = "reviewed_by")
    private String reviewedBy; // manager

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status")
    private ApprovalStatus approvalStatus;

    @Column(name = "approval_reason", columnDefinition = "TEXT")
    private String approvalReason;

    @Column(name = "issued_quantity")
    @Builder.Default
    private Integer issuedQuantity = 0;

    @Column(name = "issued_at")
    private LocalDateTime issuedAt;

    @Column(name = "issued_by")
    private String issuedBy;

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
        return requestStatus == ExportRequestStatus.PENDING;
    }

    public boolean isApproved() {
        return approvalStatus == ApprovalStatus.APPROVED;
    }

    public boolean isRejected() {
        return approvalStatus == ApprovalStatus.REJECTED;
    }

    public boolean isIssued() {
        return requestStatus == ExportRequestStatus.ISSUED;
    }

    public void approve(String reviewedBy, String reason) {
        this.approvalStatus = ApprovalStatus.APPROVED;
        this.requestStatus = ExportRequestStatus.APPROVED;
        this.reviewedBy = reviewedBy;
        this.reviewedAt = LocalDateTime.now();
        this.approvalReason = reason;
    }

    public void reject(String reviewedBy, String reason) {
        this.approvalStatus = ApprovalStatus.REJECTED;
        this.requestStatus = ExportRequestStatus.REJECTED;
        this.reviewedBy = reviewedBy;
        this.reviewedAt = LocalDateTime.now();
        this.approvalReason = reason;
    }

    public void issue(Integer issuedQuantity, String issuedBy) {
        this.requestStatus = ExportRequestStatus.ISSUED;
        this.issuedQuantity = issuedQuantity;
        this.issuedAt = LocalDateTime.now();
        this.issuedBy = issuedBy;
    }

    public void cancel() {
        this.requestStatus = ExportRequestStatus.CANCELLED;
    }

    public boolean canBeIssued() {
        return isApproved() && !isIssued();
    }

    /**
     * Custom equals method that only uses ID to avoid circular dependencies.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        SparePartsExportRequest request = (SparePartsExportRequest) obj;
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
