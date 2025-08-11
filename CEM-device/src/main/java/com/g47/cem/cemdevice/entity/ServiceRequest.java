package com.g47.cem.cemdevice.entity;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.g47.cem.cemdevice.enums.ServiceRequestStatus;
import com.g47.cem.cemdevice.enums.ServiceRequestType;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ServiceRequest entity representing customer service requests for maintenance or warranty
 */
@Entity
@Table(name = "service_requests")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ServiceRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_id", nullable = false, unique = true)
    private String requestId; // Auto-generated request ID

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    private CustomerDevice device;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ServiceRequestType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Builder.Default
    private ServiceRequestStatus status = ServiceRequestStatus.PENDING;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(name = "preferred_date_time")
    private LocalDateTime preferredDateTime;

    @Column(name = "attachments", columnDefinition = "TEXT")
    private String attachments; // JSON array of Google Drive file IDs

    @Column(name = "staff_notes", columnDefinition = "TEXT")
    private String staffNotes;

    @Column(name = "customer_comments", columnDefinition = "TEXT")
    private String customerComments;

    @Column(name = "estimated_cost", precision = 15, scale = 2)
    private java.math.BigDecimal estimatedCost;

    @Column(name = "actual_cost", precision = 15, scale = 2)
    private java.math.BigDecimal actualCost;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "rejected_by")
    private String rejectedBy;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    @Column(name = "approved_by")
    private String approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationships
    @OneToMany(mappedBy = "serviceRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ServiceRequestHistory> history;

    // Helper methods
    public boolean isPending() {
        return status == ServiceRequestStatus.PENDING;
    }

    public boolean isApproved() {
        return status == ServiceRequestStatus.APPROVED;
    }

    public boolean isInProgress() {
        return status == ServiceRequestStatus.IN_PROGRESS;
    }

    public boolean isCompleted() {
        return status == ServiceRequestStatus.COMPLETED;
    }

    public boolean isRejected() {
        return status == ServiceRequestStatus.REJECTED;
    }

    public boolean isMaintenanceRequest() {
        return type == ServiceRequestType.MAINTENANCE;
    }

    public boolean isWarrantyRequest() {
        return type == ServiceRequestType.WARRANTY;
    }
} 