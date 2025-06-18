package com.g47.cem.cemdevice.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.g47.cem.cemdevice.enums.DeviceStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Device response DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeviceResponse {
    
    private Long id;
    private String name;
    private String model;
    private String serialNumber;
    private Long customerId;
    private LocalDate warrantyExpiry;
    private DeviceStatus status;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<DeviceNoteResponse> notes;
    private List<CustomerDeviceResponse> customerDevices;
} 