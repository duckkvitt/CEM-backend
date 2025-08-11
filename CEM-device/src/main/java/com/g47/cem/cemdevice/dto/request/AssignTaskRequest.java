package com.g47.cem.cemdevice.dto.request;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for assigning a task to a technician
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AssignTaskRequest {
    
    @NotNull(message = "Technician ID is required")
    private Long technicianId;
    
    private LocalDateTime scheduledDate;
    
    @Size(max = 2000, message = "TechLead notes must not exceed 2000 characters")
    private String techleadNotes;
}
