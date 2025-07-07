package com.g47.cem.cemspareparts.dto.response;

import com.g47.cem.cemspareparts.enums.SparePartStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SparePartResponse {
    private Long id;
    private String partName;
    private String partCode;
    private String description;
    private String compatibleDevices;
    private int quantityInStock;
    private String unitOfMeasurement;
    private String supplier;
    private SparePartStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 