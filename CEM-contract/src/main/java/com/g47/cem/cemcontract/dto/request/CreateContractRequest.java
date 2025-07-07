package com.g47.cem.cemcontract.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;
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
    
    // Staff ID is optional - if not provided, will use current user from JWT token
    @Positive(message = "Staff ID must be positive")
    private Long staffId;
    
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
    
    // Điều 2: Thanh toán
    @Size(max = 255, message = "Payment method must not exceed 255 characters")
    private String paymentMethod; // Hình thức thanh toán
    
    @Size(max = 500, message = "Payment term must not exceed 500 characters")
    private String paymentTerm; // Thời hạn thanh toán
    
    @Size(max = 500, message = "Bank account must not exceed 500 characters")
    private String bankAccount; // Tài khoản ngân hàng
    
    // Điều 3: Thời gian, địa điểm, phương thức giao hàng - now managed as a table
    @Valid
    private List<CreateDeliveryScheduleRequest> deliverySchedules = new ArrayList<>();
    
    // Điều 5: Bảo hành và hướng dẫn sử dụng hàng hóa
    @Size(max = 500, message = "Warranty product must not exceed 500 characters")
    private String warrantyProduct; // Loại hàng bảo hành
    
    @Positive(message = "Warranty period must be positive")
    private Integer warrantyPeriodMonths; // Thời gian bảo hành (tháng)
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateContractDetailRequest {
        
        @NotBlank(message = "Work code is required")
        @Size(max = 100, message = "Work code must not exceed 100 characters")
        private String workCode;
        
        private Long deviceId; // Optional - can be null
        
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
    
    /**
     * DTO for creating delivery schedule items (Điều 3 table)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateDeliveryScheduleRequest {
        
        @NotBlank(message = "Item name is required")
        @Size(max = 500, message = "Item name must not exceed 500 characters")
        private String itemName; // Tên hàng hóa
        
        @NotBlank(message = "Unit is required")
        @Size(max = 50, message = "Unit must not exceed 50 characters")
        private String unit; // Đơn vị
        
        @Positive(message = "Quantity must be positive")
        private Integer quantity; // Số lượng
        
        @Size(max = 255, message = "Delivery time must not exceed 255 characters")
        private String deliveryTime; // Thời gian giao hàng
        
        @Size(max = 2000, message = "Delivery location must not exceed 2000 characters")
        private String deliveryLocation; // Địa điểm giao hàng
        
        @Size(max = 1000, message = "Notes must not exceed 1000 characters")
        private String notes; // Ghi chú
    }
} 