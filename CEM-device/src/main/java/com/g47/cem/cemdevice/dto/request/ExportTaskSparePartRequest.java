package com.g47.cem.cemdevice.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.Min;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExportTaskSparePartRequest {
    @NotNull
    private Long taskId;

    @NotNull
    private Long sparePartId;

    @NotNull
    @Min(1)
    @JsonAlias({"quantity"})
    private Integer quantityUsed;

    private BigDecimal unitPrice;

    private String notes;
}


