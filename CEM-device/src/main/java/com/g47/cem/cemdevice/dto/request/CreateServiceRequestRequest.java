package com.g47.cem.cemdevice.dto.request;

import java.time.LocalDateTime;
import java.util.List;

import com.g47.cem.cemdevice.enums.ServiceRequestType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a service request
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateServiceRequestRequest {
    
    @NotNull(message = "Device ID is required")
    private Long deviceId;
    
    @NotNull(message = "Service request type is required")
    private ServiceRequestType type;
    
    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 2000, message = "Description must be between 10 and 2000 characters")
    private String description;
    
    private LocalDateTime preferredDateTime;
    
    private List<String> attachments; // Google Drive file IDs
    
    @Size(max = 1000, message = "Customer comments must not exceed 1000 characters")
    private String customerComments;
} 