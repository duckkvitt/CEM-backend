package com.g47.cem.cemdevice.enums;

/**
 * Enum for task statuses in the workflow
 */
public enum TaskStatus {
    /**
     * Task created by Support Team, waiting for TechLead assignment
     */
    PENDING,
    
    /**
     * Task assigned by TechLead to a Technician
     */
    ASSIGNED,
    
    /**
     * Task accepted by the assigned Technician
     */
    ACCEPTED,
    
    /**
     * Task is being worked on by the Technician
     */
    IN_PROGRESS,
    
    /**
     * Task completed by the Technician
     */
    COMPLETED,
    
    /**
     * Task rejected by either TechLead or Technician
     */
    REJECTED
}
