package com.g47.cem.cemdevice.dto.response;

import java.time.LocalDateTime;

import com.g47.cem.cemdevice.enums.ServiceRequestStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for service request history
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ServiceRequestHistoryResponse {
    
    private Long id;
    
    private ServiceRequestStatus status;
    
    private String comment;
    
    private String updatedBy;
    
    private LocalDateTime createdAt;
} 