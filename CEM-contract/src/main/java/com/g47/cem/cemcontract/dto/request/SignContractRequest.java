package com.g47.cem.cemcontract.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class SignContractRequest {
    
    @NotEmpty(message = "Signature data cannot be empty")
    private String signatureData;

} 