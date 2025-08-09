package com.g47.cem.cemspareparts.dto.request;

import com.g47.cem.cemspareparts.enums.SparePartStatus;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateSparePartRequest {

    @Size(max = 255, message = "Part name must be less than 255 characters")
    private String partName;

    private String description;

    @Size(max = 255, message = "Compatible devices must be less than 255 characters")
    private String compatibleDevices;

    @Size(max = 255, message = "Unit of measurement must be less than 255 characters")
    private String unitOfMeasurement;

    private SparePartStatus status;
} 