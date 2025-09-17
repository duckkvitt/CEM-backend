package com.g47.cem.cemdevice.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateCustomerFeedbackRequest {
    @NotNull
    private Long serviceRequestId;

    @NotNull
    @Min(1)
    @Max(5)
    private Integer starRating;

    private String comment;
}



