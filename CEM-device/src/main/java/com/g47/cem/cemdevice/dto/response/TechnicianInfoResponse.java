package com.g47.cem.cemdevice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for technician information
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TechnicianInfoResponse {
    
    private Long id;
    
    private String firstName;
    
    private String lastName;
    
    private String email;
    
    private String phone;
    
    private String fullName;
    
    // Task-related statistics
    private long currentTaskCount;
    
    private long totalTaskCount;
    
    private long completedTaskCount;
    
    private double averageRating;
    
    private boolean isAvailable;
    
    // Workload information
    private int workloadPercentage;
    
    private String availabilityStatus; // AVAILABLE, BUSY, OVERLOADED, UNAVAILABLE
    
    private int maxConcurrentTasks;
    
    // Skills and specializations
    private String skills;
    
    private String location;
    
    // Performance metrics
    private double completionRate;
    
    private int averageCompletionDays;
    
    private String lastActiveDate;
}
