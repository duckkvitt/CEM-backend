package com.g47.cem.cemcontract.dto.response;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for signature verification results
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignatureVerificationResult {
    
    private boolean signatureValid; // Cryptographic signature is valid
    private boolean certificateValid; // Certificate chain is valid
    private boolean timestampValid; // Timestamp is valid (if present)
    private boolean documentIntegrityValid; // Document hasn't been modified
    
    @Builder.Default
    private List<String> errors = new ArrayList<>(); // Verification errors
    
    @Builder.Default
    private List<String> warnings = new ArrayList<>(); // Verification warnings
    
    private String signerName; // Name from certificate
    private String signerEmail; // Email from certificate
    private String issuerName; // Certificate issuer
    private LocalDateTime signingTime; // When document was signed
    private LocalDateTime timestampTime; // Timestamp from TSA
    private LocalDateTime verificationTime; // When verification was performed
    
    private String signatureAlgorithm; // Algorithm used for signing
    private String hashAlgorithm; // Hash algorithm used
    private String certificateSerialNumber; // Certificate serial number
    private String certificateFingerprint; // Certificate fingerprint
    
    private boolean hasTimestamp; // Whether signature includes timestamp
    private String timestampAuthority; // TSA that provided timestamp
    
    private boolean coversWholeDocument; // Whether signature covers entire document
    private int totalSignatures; // Total number of signatures in document
    private int validSignatures; // Number of valid signatures
    
    /**
     * Check if verification is completely successful
     */
    public boolean isValid() {
        return signatureValid && 
               certificateValid && 
               documentIntegrityValid && 
               (timestampValid || !hasTimestamp) &&
               errors.isEmpty();
    }
    
    /**
     * Add an error message
     */
    public void addError(String error) {
        if (errors == null) {
            errors = new ArrayList<>();
        }
        errors.add(error);
    }
    
    /**
     * Add a warning message
     */
    public void addWarning(String warning) {
        if (warnings == null) {
            warnings = new ArrayList<>();
        }
        warnings.add(warning);
    }
    
    /**
     * Get summary status
     */
    public String getStatusSummary() {
        if (isValid()) {
            return "VALID";
        } else if (signatureValid && certificateValid) {
            return "PARTIALLY_VALID";
        } else {
            return "INVALID";
        }
    }
    
    /**
     * Get detailed status message
     */
    public String getDetailedStatus() {
        StringBuilder status = new StringBuilder();
        
        if (isValid()) {
            status.append("Signature is completely valid");
            if (hasTimestamp) {
                status.append(" and timestamped");
            }
        } else {
            status.append("Signature verification failed: ");
            if (!errors.isEmpty()) {
                status.append(String.join(", ", errors));
            }
        }
        
        return status.toString();
    }
} 