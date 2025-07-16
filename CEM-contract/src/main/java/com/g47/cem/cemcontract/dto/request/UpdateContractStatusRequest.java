package com.g47.cem.cemcontract.dto.request;

import com.g47.cem.cemcontract.enums.ContractStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateContractStatusRequest {

    @NotNull(message = "Status cannot be null")
    private ContractStatus status;

    private String comment;

    public com.g47.cem.cemcontract.enums.ContractStatus getStatus() {
        return status;
    }
} 