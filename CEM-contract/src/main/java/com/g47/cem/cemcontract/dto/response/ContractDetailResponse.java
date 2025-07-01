package com.g47.cem.cemcontract.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for contract detail (line item) data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ContractDetailResponse {
    
    private Long id;
    private Long contractId;
    private String workCode;
    private Long deviceId;
    private String deviceName; // Populated by service
    
    private String serviceName;
    private String description;
    
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    
    private Integer warrantyMonths;
    private String notes;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 