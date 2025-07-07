package com.g47.cem.cemspareparts.dto.request;

import com.g47.cem.cemspareparts.enums.SparePartStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateSparePartRequest {

    @Size(max = 255, message = "Part name must be less than 255 characters")
    private String partName;

    private String description;

    @Size(max = 255, message = "Compatible devices must be less than 255 characters")
    private String compatibleDevices;

    @Min(value = 0, message = "Quantity in stock cannot be negative")
    private Integer quantityInStock;

    @Size(max = 255, message = "Unit of measurement must be less than 255 characters")
    private String unitOfMeasurement;

    @Size(max = 255, message = "Supplier must be less than 255 characters")
    private String supplier;

    private SparePartStatus status;
} 