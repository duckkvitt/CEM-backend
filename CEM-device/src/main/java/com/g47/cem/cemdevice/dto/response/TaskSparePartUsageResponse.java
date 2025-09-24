package com.g47.cem.cemdevice.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TaskSparePartUsageResponse {
    private Long id;
    private Long taskId;
    private Long sparePartId;
    private String sparePartName;
    private String sparePartCode;
    private Integer quantityUsed;
    private BigDecimal unitPrice;
    private BigDecimal totalCost;
    private String notes;
    private LocalDateTime usedAt;
    private String createdBy;
}


