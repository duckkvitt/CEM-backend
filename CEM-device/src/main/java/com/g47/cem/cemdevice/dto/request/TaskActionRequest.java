package com.g47.cem.cemdevice.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for task actions (accept, reject, complete)
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskActionRequest {
    
    @Size(max = 2000, message = "Comment must not exceed 2000 characters")
    private String comment;
    
    @Size(max = 2000, message = "Rejection reason must not exceed 2000 characters")
    private String rejectionReason;
}
