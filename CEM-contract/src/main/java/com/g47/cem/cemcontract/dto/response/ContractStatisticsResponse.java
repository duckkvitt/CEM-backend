package com.g47.cem.cemcontract.dto.response;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for contract statistics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ContractStatisticsResponse {
    
    // Count statistics
    private Long totalContracts;
    private Long unsignedContracts;
    private Long paperSignedContracts;
    private Long digitallySignedContracts;
    private Long cancelledContracts;
    private Long expiredContracts;
    private Long hiddenContracts;
    private Long expiringSoonContracts; // Expiring within 30 days
    
    // Value statistics
    private BigDecimal totalContractValue;
    private BigDecimal unsignedContractValue;
    private BigDecimal signedContractValue;
    
    // Monthly statistics
    private Long contractsThisMonth;
    private Long contractsLastMonth;
    private BigDecimal revenueThisMonth;
    private BigDecimal revenueLastMonth;
    
    // Growth rates (percentage)
    private Double contractGrowthRate;
    private Double revenueGrowthRate;
} 