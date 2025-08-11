package com.g47.cem.cemdevice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for rejecting a service request
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RejectServiceRequestRequest {
    
    @NotBlank(message = "Rejection reason is required")
    @Size(min = 10, max = 2000, message = "Rejection reason must be between 10 and 2000 characters")
    private String rejectionReason;
}
