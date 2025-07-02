package com.g47.cem.cemcontract.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a new contract
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateContractRequest {
    
    @NotNull(message = "Customer ID is required")
    @Positive(message = "Customer ID must be positive")
    private Long customerId;
    
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
     */
    private List<CreateContractDetailRequest> contractDetails;
    
    /**
     * Đường dẫn (public_id) file hợp đồng đã upload lên Cloudinary hoặc tên file lưu local.
     */
    private String filePath;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateContractDetailRequest {
        
        @NotBlank(message = "Work code is required")
        @Size(max = 100, message = "Work code must not exceed 100 characters")
        private String workCode;
        
        private Long deviceId; // Optional - can be null
        
        @NotBlank(message = "Service name is required")
        @Size(max = 255, message = "Service name must not exceed 255 characters")
        private String serviceName;
        
        @Size(max = 1000, message = "Description must not exceed 1000 characters")
        private String description;
        
        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be positive")
        private Integer quantity;
        
        @NotNull(message = "Unit price is required")
        @Positive(message = "Unit price must be positive")
        private BigDecimal unitPrice;
        
        private Integer warrantyMonths;
        
        @Size(max = 500, message = "Notes must not exceed 500 characters")
        private String notes;
    }
} 