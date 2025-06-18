package com.g47.cem.cemdevice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a device note
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateDeviceNoteRequest {
    
    @NotBlank(message = "Note content is required")
    @Size(min = 1, max = 5000, message = "Note must be between 1 and 5000 characters")
    private String note;
} 