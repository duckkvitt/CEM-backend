package com.g47.cem.cemcontract.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.g47.cem.cemcontract.enums.ContractStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for contract summary data (used in listings)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ContractSummaryResponse {
    
    private Long id;
    private String contractNumber;
    private Long customerId;
    private String customerName;
    private Long staffId;
    private String staffName;
    
    private String title;
    private BigDecimal totalValue;
    private LocalDate startDate;
    private LocalDate endDate;
    
    private ContractStatus status;
    private Boolean digitalSigned;
    private Boolean paperConfirmed;
    private Boolean isHidden;
    
    private LocalDateTime createdAt;
    private LocalDateTime signedAt;
    
    // Additional computed fields
    private Integer daysUntilExpiry;
    private Boolean isExpiringSoon;
    private Integer totalLineItems;
} 