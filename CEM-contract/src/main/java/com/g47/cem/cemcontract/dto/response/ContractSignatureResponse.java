package com.g47.cem.cemcontract.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.g47.cem.cemcontract.enums.SignatureType;
import com.g47.cem.cemcontract.enums.SignerType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for contract signature data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ContractSignatureResponse {
    
    private Long id;
    private Long contractId;
    
    private SignatureType signatureType;
    private SignerType signerType;
    
    private String signerName;
    private String signerEmail;
    private Long signerId; // User ID if applicable
    
    private String signatureData; // Can be file path or base64 data
    private String ipAddress;
    private String userAgent;
    private String notes;
    
    private LocalDateTime signedAt;
} 