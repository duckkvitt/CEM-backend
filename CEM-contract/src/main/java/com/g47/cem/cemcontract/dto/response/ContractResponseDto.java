package com.g47.cem.cemcontract.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.g47.cem.cemcontract.enums.ContractStatus;

import lombok.Data;

@Data
public class ContractResponseDto {
    private Long id;
    private String contractNumber;
    private Long customerId;
    private String customerName; // Add customer name for display
    private Long staffId;
    private String title;
    private String description;
    private ContractStatus status;
    private String filePath;
    private BigDecimal totalValue;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime createdAt;
    private Boolean isHidden; // Add this field to track hidden status
    
    // Điều 2: Thanh toán
    private String paymentMethod; // Hình thức thanh toán
    private String paymentTerm; // Thời hạn thanh toán
    private String bankAccount; // Tài khoản ngân hàng
    
    // Điều 3: Thời gian, địa điểm, phương thức giao hàng - now managed as a table
    private List<DeliveryScheduleDto> deliverySchedules = new ArrayList<>();
    
    // Điều 5: Bảo hành và hướng dẫn sử dụng hàng hóa
    private String warrantyProduct; // Loại hàng bảo hành
    private Integer warrantyPeriodMonths; // Thời gian bảo hành (tháng)
    
    private List<ContractDetailDto> contractDetails;

    @Data
    public static class ContractDetailDto {
        private Long id;
        private String deviceName;
        private int quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
    }
    
    /**
     * DTO for delivery schedule items in response
     */
    @Data
    public static class DeliveryScheduleDto {
        private Long id;
        private Integer sequenceNumber;
        private String itemName; // Tên hàng hóa
        private String unit; // Đơn vị
        private Integer quantity; // Số lượng
        private String deliveryTime; // Thời gian giao hàng
        private String deliveryLocation; // Địa điểm giao hàng
        private String notes; // Ghi chú
    }

    public String getFilePath() {
        return filePath;
    }
} 