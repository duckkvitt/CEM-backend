package com.g47.cem.cemdevice.dto.request;

import com.g47.cem.cemdevice.enums.TaskStatus;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating task status
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTaskStatusRequest {
    @NotNull
    private TaskStatus status;
    private String comment;
}



