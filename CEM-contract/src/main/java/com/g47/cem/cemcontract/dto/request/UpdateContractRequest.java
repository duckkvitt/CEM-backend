package com.g47.cem.cemcontract.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating a contract
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateContractRequest {
    
    @NotBlank(message = "Contract title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;
    
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;
    
    @Positive(message = "Total value must be positive")
    private BigDecimal totalValue;
    
    private LocalDate startDate;
    
    @Future(message = "End date must be in the future")
    private LocalDate endDate;
    
    /**
     * List of contract details (services/items)
     * If provided, this will replace all existing contract details
     */
    private List<CreateContractRequest.CreateContractDetailRequest> contractDetails;
    
    /**
     * Updated file path for the contract document (Cloudinary public_id or local filename).
     */
    private String filePath;
} 