package com.g47.cem.cemdevice.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.g47.cem.cemdevice.enums.ServiceRequestStatus;
import com.g47.cem.cemdevice.enums.ServiceRequestType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for service requests
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ServiceRequestResponse {
    
    private Long id;
    
    private String requestId;
    
    private Long customerId;
    
    private Long deviceId;
    
    private String deviceName;
    
    private String deviceModel;
    
    private String serialNumber;
    
    private ServiceRequestType type;
    
    private ServiceRequestStatus status;
    
    private String description;
    
    private LocalDateTime preferredDateTime;
    
    private List<String> attachments;
    
    private String staffNotes;
    
    private String customerComments;
    
    private BigDecimal estimatedCost;
    
    private BigDecimal actualCost;
    
    private LocalDateTime completedAt;
    
    private String createdBy;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private List<ServiceRequestHistoryResponse> history;
} 