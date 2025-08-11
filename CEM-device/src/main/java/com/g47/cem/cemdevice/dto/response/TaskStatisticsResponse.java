package com.g47.cem.cemdevice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for task statistics
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskStatisticsResponse {
    
    private long totalTasks;
    
    private long pendingTasks;
    
    private long assignedTasks;
    
    private long acceptedTasks;
    
    private long inProgressTasks;
    
    private long completedTasks;
    
    private long rejectedTasks;
    
    private long highPriorityTasks;
    
    private long criticalPriorityTasks;
    
    private long overdueTasks;
    
    private double completionRate;
    
    private double rejectionRate;
}
