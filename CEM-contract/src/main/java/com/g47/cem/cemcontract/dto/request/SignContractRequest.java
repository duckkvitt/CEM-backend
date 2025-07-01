package com.g47.cem.cemcontract.dto.request;

import com.g47.cem.cemcontract.enums.SignatureType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for signing a contract
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignContractRequest {
    
    @NotNull(message = "Signature type is required")
    private SignatureType signatureType;
    
    @NotBlank(message = "Signer name is required")
    private String signerName;
    
    @NotBlank(message = "Signer email is required")
    private String signerEmail;
    
    /**
     * Base64 encoded signature data or file path
     * Required for digital signatures
     */
    private String signatureData;
    
    /**
     * Reason for signing or additional notes
     */
    private String notes;
} 