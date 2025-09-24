package com.g47.cem.cemspareparts.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ExportSparePartForTaskRequest {
    @NotNull
    private Long taskId;

    @NotNull
    private Long sparePartId;

    @NotNull
    @Min(1)
    private Integer quantity;

    private BigDecimal unitPrice;

    private String notes;
}


