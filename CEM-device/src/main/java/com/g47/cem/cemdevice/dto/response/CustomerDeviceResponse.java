package com.g47.cem.cemdevice.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.g47.cem.cemdevice.enums.CustomerDeviceStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDeviceResponse {
    private Long id;
    private Long customerId;
    private Long deviceId;
    private String deviceName;
    private LocalDate warrantyEnd;
    private CustomerDeviceStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 