package com.g47.cem.cemdevice.dto.response;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CustomerFeedbackResponse {
    private Long id;
    private Long serviceRequestId;
    private String serviceRequestCode;
    private Long customerId;
    private String customerName;
    private Long deviceId;
    private String deviceName;
    private String deviceType;
    private String serviceType;
    private Integer starRating;
    private String comment;
    private Long technicianId;
    private String technicianName;
    private LocalDateTime submittedAt;
}



