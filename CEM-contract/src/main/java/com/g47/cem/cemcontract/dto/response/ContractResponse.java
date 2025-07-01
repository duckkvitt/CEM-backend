package com.g47.cem.cemcontract.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.g47.cem.cemcontract.enums.ContractStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for contract data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ContractResponse {
    
    private Long id;
    private String contractNumber;
    private Long customerId;
    private String customerName; // Populated by service
    private Long staffId;
    private String staffName; // Populated by service
    
    private String title;
    private String description;
    
    private BigDecimal totalValue;
    private LocalDate startDate;
    private LocalDate endDate;
    
    private ContractStatus status;
    private String filePath;
    private Boolean digitalSigned;
    private Boolean paperConfirmed;
    private Boolean isHidden;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime signedAt;
    
    /**
     * Contract details/line items
     */
    private List<ContractDetailResponse> contractDetails;
    
    /**
     * Signature history
     */
    private List<ContractSignatureResponse> signatures;
    
    // Additional fields for specific responses
    private Integer daysUntilExpiry;
    private Boolean isExpiringSoon;
} 