package com.g47.cem.cemcontract.dto.request;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.g47.cem.cemcontract.enums.ContractStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for searching contracts with various filters
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractSearchRequest {
    
    /**
     * Search term for title, description, or contract number
     */
    private String searchTerm;
    
    /**
     * Filter by customer ID
     */
    private Long customerId;
    
    /**
     * Filter by staff ID
     */
    private Long staffId;
    
    /**
     * Filter by contract status
     */
    private ContractStatus status;
    
    /**
     * Filter by creation date range
     */
    private LocalDateTime createdAfter;
    private LocalDateTime createdBefore;
    
    /**
     * Filter by signing date range
     */
    private LocalDateTime signedAfter;
    private LocalDateTime signedBefore;
    
    /**
     * Filter by contract date range
     */
    private LocalDate startDateAfter;
    private LocalDate startDateBefore;
    private LocalDate endDateAfter;
    private LocalDate endDateBefore;
    
    /**
     * Include hidden contracts
     */
    @Builder.Default
    private Boolean includeHidden = false;
    
    /**
     * Only show signed contracts
     */
    private Boolean signedOnly;
    
    /**
     * Only show expiring contracts (within specified days)
     */
    private Integer expiringWithinDays;
} 