package com.g47.cem.cemdevice.enums;

/**
 * Enum for service request statuses
 */
public enum ServiceRequestStatus {
    /**
     * Initial status when customer submits the request
     */
    PENDING,
    
    /**
     * Approved by Support Team and converted to task
     */
    APPROVED,
    
    /**
     * Task has been created and assigned to a technician
     */
    ASSIGNED,
    
    /**
     * Rejected by Support Team
     */
    REJECTED,
    
    /**
     * Being worked on by technician (via task)
     */
    IN_PROGRESS,
    
    /**
     * Completed by technician
     */
    COMPLETED
} 