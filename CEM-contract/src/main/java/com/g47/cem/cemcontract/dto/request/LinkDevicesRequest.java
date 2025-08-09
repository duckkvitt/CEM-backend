package com.g47.cem.cemcontract.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LinkDevicesRequest {
    private Long contractId;
    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotNull(message = "Device information is required")
    private List<DeviceInfo> devices;

    @Data
    public static class DeviceInfo {
        @NotNull(message = "Device ID is required")
        private Long deviceId;
        private Integer quantity = 1;
        private Integer warrantyMonths = 0;
    }
} 