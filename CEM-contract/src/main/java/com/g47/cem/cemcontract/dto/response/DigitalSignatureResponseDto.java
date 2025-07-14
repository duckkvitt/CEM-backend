package com.g47.cem.cemcontract.dto.response;

import com.g47.cem.cemcontract.enums.SignatureStatus;
import com.g47.cem.cemcontract.enums.SignerType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DigitalSignatureResponseDto {
    
    private Long id;
    private Long contractId;
    private String contractNumber;
    private String signerName;
    private String signerEmail;
    private SignerType signerType;
    private SignatureStatus status;
    private String signatureAlgorithm;
    private String reason;
    private String location;
    private LocalDateTime signedAt;
    private String ipAddress;
    
    // Certificate info
    private String certificateSubjectDN;
    private String certificateIssuerDN;
    private String certificateSerialNumber;
    
    // PDF info
    private Integer pageNumber;
    private Double signatureX;
    private Double signatureY;
    private Double signatureWidth;
    private Double signatureHeight;
} 