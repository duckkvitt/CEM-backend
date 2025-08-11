package com.g47.cem.cemdevice.dto.response;

import java.time.LocalDateTime;

import com.g47.cem.cemdevice.enums.TaskStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for task history
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskHistoryResponse {
    
    private Long id;
    
    private Long taskId;
    
    private TaskStatus status;
    
    private String comment;
    
    private String updatedBy;
    
    private String userRole;
    
    private LocalDateTime createdAt;
}
