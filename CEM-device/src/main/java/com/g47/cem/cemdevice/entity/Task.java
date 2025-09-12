package com.g47.cem.cemdevice.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.g47.cem.cemdevice.enums.TaskPriority;
import com.g47.cem.cemdevice.enums.TaskStatus;
import com.g47.cem.cemdevice.enums.TaskType;

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
 * Task entity representing work tasks assigned to technicians
 */
@Entity
@Table(name = "tasks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_id", nullable = false, unique = true)
    private String taskId; // Auto-generated task ID

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TaskType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Builder.Default
    private TaskStatus status = TaskStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Builder.Default
    private TaskPriority priority = TaskPriority.NORMAL;

    // Relationships
    @Column(name = "service_request_id")
    private Long serviceRequestId; // Optional - tasks can be created from service requests or manually

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_device_id", nullable = false)
    private CustomerDevice customerDevice;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    // Assignment information
    @Column(name = "assigned_technician_id")
    private Long assignedTechnicianId;

    @Column(name = "assigned_by")
    private String assignedBy; // TechLead who assigned the task

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    // Scheduling
    @Column(name = "scheduled_date")
    private LocalDateTime scheduledDate;

    @Column(name = "estimated_duration_hours")
    private Integer estimatedDurationHours;

    // Location and service details
    @Column(name = "service_location", columnDefinition = "TEXT")
    private String serviceLocation;

    @Column(name = "customer_contact_info", columnDefinition = "TEXT")
    private String customerContactInfo;

    @Column(name = "actual_cost", precision = 15, scale = 2)
    private BigDecimal actualCost;

    // Notes and comments
    @Column(name = "support_notes", columnDefinition = "TEXT")
    private String supportNotes; // Notes from Support Team

    @Column(name = "techlead_notes", columnDefinition = "TEXT")
    private String techleadNotes; // Notes from TechLead

    @Column(name = "technician_notes", columnDefinition = "TEXT")
    private String technicianNotes; // Notes from assigned Technician

    @Column(name = "completion_notes", columnDefinition = "TEXT")
    private String completionNotes; // Final completion notes

    // Rejection information
    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "rejected_by")
    private String rejectedBy;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    // Timestamps
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationships
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TaskHistory> history;

    // Helper methods
    public boolean isPending() {
        return status == TaskStatus.PENDING;
    }

    public boolean isAssigned() {
        return status == TaskStatus.ASSIGNED;
    }

    public boolean isAccepted() {
        return status == TaskStatus.ACCEPTED;
    }

    public boolean isInProgress() {
        return status == TaskStatus.IN_PROGRESS;
    }

    public boolean isCompleted() {
        return status == TaskStatus.COMPLETED;
    }

    public boolean isRejected() {
        return status == TaskStatus.REJECTED;
    }

    public boolean isHighPriority() {
        return priority == TaskPriority.HIGH || priority == TaskPriority.CRITICAL;
    }

    public boolean isMaintenanceTask() {
        return type == TaskType.MAINTENANCE || type == TaskType.PREVENTIVE_MAINTENANCE;
    }

    public boolean isWarrantyTask() {
        return type == TaskType.WARRANTY;
    }

    public boolean hasAssignedTechnician() {
        return assignedTechnicianId != null;
    }

    public boolean canBeAssigned() {
        return status == TaskStatus.PENDING;
    }

    public boolean canBeAcceptedOrRejected() {
        return status == TaskStatus.ASSIGNED;
    }

    public boolean canBeWorkedOn() {
        return status == TaskStatus.ACCEPTED || status == TaskStatus.IN_PROGRESS;
    }
}
