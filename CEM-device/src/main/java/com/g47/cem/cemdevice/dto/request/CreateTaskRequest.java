package com.g47.cem.cemdevice.dto.request;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.g47.cem.cemdevice.enums.TaskPriority;
import com.g47.cem.cemdevice.enums.TaskType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a task
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateTaskRequest {
    
    @NotBlank(message = "Title is required")
    @Size(min = 5, max = 255, message = "Title must be between 5 and 255 characters")
    private String title;
    
    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 2000, message = "Description must be between 10 and 2000 characters")
    private String description;
    
    @NotNull(message = "Task type is required")
    private TaskType type;
    
    @Builder.Default
    private TaskPriority priority = TaskPriority.NORMAL;
    
    @NotNull(message = "Customer device ID is required")
    private Long customerDeviceId;
    
    @NotNull(message = "Customer ID is required")
    private Long customerId;
    
    private LocalDateTime scheduledDate;
    
    @Positive(message = "Estimated duration must be positive")
    private Integer estimatedDurationHours;
    
    @Size(max = 1000, message = "Service location must not exceed 1000 characters")
    private String serviceLocation;
    
    @Size(max = 500, message = "Customer contact info must not exceed 500 characters")
    private String customerContactInfo;
    
    @Positive(message = "Estimated cost must be positive")
    private BigDecimal estimatedCost;
    
    @Size(max = 2000, message = "Support notes must not exceed 2000 characters")
    private String supportNotes;
}
