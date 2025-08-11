package com.g47.cem.cemdevice.dto.response;

import java.time.LocalDateTime;

import com.g47.cem.cemdevice.enums.TaskPriority;
import com.g47.cem.cemdevice.enums.TaskStatus;
import com.g47.cem.cemdevice.enums.TaskType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for technician work schedule
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TechnicianWorkScheduleResponse {
    
    private Long taskId;
    
    private String taskNumber;
    
    private String title;
    
    private TaskType type;
    
    private TaskStatus status;
    
    private TaskPriority priority;
    
    private LocalDateTime scheduledDate;
    
    private Integer estimatedDurationHours;
    
    private String serviceLocation;
    
    private String customerName;
    
    private String customerPhone;
    
    private String customerEmail;
    
    private String deviceName;
    
    private String deviceModel;
    
    private String serialNumber;
    
    private String description;
    
    private LocalDateTime createdAt;
}
