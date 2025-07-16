package com.g47.cem.cemcontract.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for digital signature requests
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DigitalSignatureRequest {
    
    @NotNull(message = "Contract ID is required")
    private Long contractId;
    
    private Long certificateId; // Optional, will use default if not provided
    
    @NotBlank(message = "Signer type is required")
    private String signerType; // MANAGER, CUSTOMER, STAFF
    
    private Long signerId; // User ID or Customer ID
    
    @NotBlank(message = "Signer name is required")
    private String signerName;
    
    @Email(message = "Valid email is required")
    @NotBlank(message = "Signer email is required")
    private String signerEmail;
    
    @NotBlank(message = "Signature data is required")
    private String signatureData; // Base64 encoded PNG from canvas
    
    // Optional signature position
    private Integer pageNumber; // PDF page number (1-based)
    private Float signatureX; // X coordinate
    private Float signatureY; // Y coordinate
    private Float signatureWidth; // Signature width
    private Float signatureHeight; // Signature height
    
    // Signature metadata
    private String reason; // Reason for signing
    private String location; // Geographic location
    private String contactInfo; // Contact information
    
    // Audit information
    private String ipAddress; // Client IP address
    private String userAgent; // Browser user agent
    
    // Timestamp options
    private Boolean includeTimestamp; // Whether to include RFC 3161 timestamp
    private String timestampUrl; // Custom TSA URL (optional)

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
    public String getSignerName() {
        return signerName;
    }
    public void setSignerName(String signerName) {
        this.signerName = signerName;
    }
    public String getSignerEmail() {
        return signerEmail;
    }
    public void setSignerEmail(String signerEmail) {
        this.signerEmail = signerEmail;
    }
} 