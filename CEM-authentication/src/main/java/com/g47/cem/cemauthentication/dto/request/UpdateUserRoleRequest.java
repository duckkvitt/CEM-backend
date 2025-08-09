package com.g47.cem.cemauthentication.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateUserRoleRequest {
    
    @NotNull(message = "Role ID is required")
    private Long roleId;
}