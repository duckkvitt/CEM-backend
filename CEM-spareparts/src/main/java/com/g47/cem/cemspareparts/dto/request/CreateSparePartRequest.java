package com.g47.cem.cemspareparts.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateSparePartRequest {

    @NotBlank(message = "Part name is required")
    @Size(max = 255, message = "Part name must be less than 255 characters")
    private String partName;

    @NotBlank(message = "Part code is required")
    @Size(max = 255, message = "Part code must be less than 255 characters")
    private String partCode;

    private String description;

    @Size(max = 255, message = "Compatible devices must be less than 255 characters")
    private String compatibleDevices;

    @NotBlank(message = "Unit of measurement is required")
    @Size(max = 255, message = "Unit of measurement must be less than 255 characters")
    private String unitOfMeasurement;
} 