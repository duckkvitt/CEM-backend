package com.g47.cem.cemdevice.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.g47.cem.cemdevice.enums.TaskPriority;
import com.g47.cem.cemdevice.enums.TaskStatus;
import com.g47.cem.cemdevice.enums.TaskType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for tasks
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskResponse {
    
    private Long id;
    
    private String taskId;
    
    private String title;
    
    private String description;
    
    private TaskType type;
    
    private TaskStatus status;
    
    private TaskPriority priority;
    
    // Relationships
    private Long serviceRequestId;
    
    private String serviceRequestNumber;
    
    private Long customerDeviceId;
    
    private String deviceName;
    
    private String deviceModel;
    
    private String serialNumber;
    
    private Long customerId;
    
    private String customerName;
    
    private String customerEmail;
    
    private String customerPhone;
    
    // Assignment information
    private Long assignedTechnicianId;
    
    private String assignedTechnicianName;
    
    private String assignedTechnicianEmail;
    
    private String assignedBy;
    
    private LocalDateTime assignedAt;
    
    // Scheduling
    private LocalDateTime scheduledDate;
    
    private LocalDateTime preferredCompletionDate;
    
    private Integer estimatedDurationHours;
    
    // Location and service details
    private String serviceLocation;
    
    private String customerContactInfo;
    
    // Cost information
    private BigDecimal estimatedCost;
    
    private BigDecimal actualCost;
    
    // Notes and comments
    private String supportNotes;
    
    private String techleadNotes;
    
    private String technicianNotes;
    
    private String completionNotes;
    
    // Rejection information
    private String rejectionReason;
    
    private String rejectedBy;
    
    private LocalDateTime rejectedAt;
    
    // Timestamps
    private LocalDateTime completedAt;
    
    private String createdBy;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    // History
    private List<TaskHistoryResponse> history;
}
