package com.g47.cem.cemdevice.dto.request;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.g47.cem.cemdevice.enums.TaskPriority;
import com.g47.cem.cemdevice.enums.TaskStatus;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating a task
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateTaskRequest {
    
    @Size(min = 5, max = 255, message = "Title must be between 5 and 255 characters")
    private String title;
    
    @Size(min = 10, max = 2000, message = "Description must be between 10 and 2000 characters")
    private String description;
    
    private TaskPriority priority;
    
    private TaskStatus status;
    
    private LocalDateTime scheduledDate;
    
    @Positive(message = "Estimated duration must be positive")
    private Integer estimatedDurationHours;
    
    @Size(max = 1000, message = "Service location must not exceed 1000 characters")
    private String serviceLocation;
    
    @Size(max = 500, message = "Customer contact info must not exceed 500 characters")
    private String customerContactInfo;
    
    @Positive(message = "Estimated cost must be positive")
    private BigDecimal estimatedCost;
    
    @Positive(message = "Actual cost must be positive")
    private BigDecimal actualCost;
    
    @Size(max = 2000, message = "Support notes must not exceed 2000 characters")
    private String supportNotes;
    
    @Size(max = 2000, message = "TechLead notes must not exceed 2000 characters")
    private String techleadNotes;
    
    @Size(max = 2000, message = "Technician notes must not exceed 2000 characters")
    private String technicianNotes;
    
    @Size(max = 2000, message = "Completion notes must not exceed 2000 characters")
    private String completionNotes;
}
