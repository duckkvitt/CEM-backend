package com.g47.cem.cemdevice.dto.request;

import java.time.LocalDateTime;

import com.g47.cem.cemdevice.enums.TaskPriority;
import com.g47.cem.cemdevice.enums.TaskType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for approving a service request and converting it to a task
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApproveServiceRequestRequest {
    
    @NotBlank(message = "Task title is required")
    @Size(min = 5, max = 255, message = "Task title must be between 5 and 255 characters")
    private String taskTitle;
    
    @Size(max = 2000, message = "Additional notes must not exceed 2000 characters")
    private String additionalNotes;
    
    @Builder.Default
    private TaskType taskType = TaskType.MAINTENANCE;
    
    @Builder.Default
    private TaskPriority priority = TaskPriority.NORMAL;
    
    private LocalDateTime scheduledDate;
    
    @Positive(message = "Estimated duration must be positive")
    private Integer estimatedDurationHours;
    
    @Size(max = 1000, message = "Service location must not exceed 1000 characters")
    private String serviceLocation;
    
    @Size(max = 500, message = "Customer contact info must not exceed 500 characters")
    private String customerContactInfo;
    
    @Size(max = 2000, message = "Support notes must not exceed 2000 characters")
    private String supportNotes;
}
