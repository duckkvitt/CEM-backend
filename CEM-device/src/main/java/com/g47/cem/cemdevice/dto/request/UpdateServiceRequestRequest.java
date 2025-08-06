package com.g47.cem.cemdevice.dto.request;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.g47.cem.cemdevice.enums.ServiceRequestStatus;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating a service request
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateServiceRequestRequest {
    
    private ServiceRequestStatus status;
    
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;
    
    private LocalDateTime preferredDateTime;
    
    private List<String> attachments; // Google Drive file IDs
    
    @Size(max = 2000, message = "Staff notes must not exceed 2000 characters")
    private String staffNotes;
    
    @Size(max = 1000, message = "Customer comments must not exceed 1000 characters")
    private String customerComments;
    
    private BigDecimal estimatedCost;
    
    private BigDecimal actualCost;
    
    private LocalDateTime completedAt;
} 