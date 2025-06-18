package com.g47.cem.cemdevice.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Device note response DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeviceNoteResponse {
    
    private Long id;
    private Long deviceId;
    private String note;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 