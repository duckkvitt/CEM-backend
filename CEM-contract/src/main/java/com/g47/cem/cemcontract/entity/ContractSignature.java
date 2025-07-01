package com.g47.cem.cemcontract.entity;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.g47.cem.cemcontract.enums.SignatureType;
import com.g47.cem.cemcontract.enums.SignerType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ContractSignature entity for tracking signature events
 */
@Entity
@Table(name = "contract_signatures")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ContractSignature {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "signer_type", nullable = false, length = 50)
    private SignerType signerType; // STAFF, CUSTOMER, MANAGER
    
    @Column(name = "signer_id")
    private Long signerId; // User ID or Customer ID
    
    @Column(name = "signer_name", nullable = false, length = 255)
    private String signerName;
    
    @Column(name = "signer_email", nullable = false, length = 255)
    private String signerEmail;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "signature_type", nullable = false, length = 50)
    private SignatureType signatureType; // DIGITAL, PAPER, DIGITAL_IMAGE
    
    @Column(name = "signature_data", columnDefinition = "TEXT")
    private String signatureData; // Base64 encoded signature or file path
    
    @Column(name = "ip_address", length = 45)
    private String ipAddress; // IP address when signed
    
    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent; // Browser info when signed
    
    @Column(name = "signed_at", nullable = false)
    @Builder.Default
    private LocalDateTime signedAt = LocalDateTime.now();
    
    // Helper methods
    
    /**
     * Check if this is a digital signature
     */
    public boolean isDigitalSignature() {
        return signatureType == SignatureType.DIGITAL || signatureType == SignatureType.DIGITAL_IMAGE;
    }
    
    /**
     * Check if this is a paper signature
     */
    public boolean isPaperSignature() {
        return signatureType == SignatureType.PAPER;
    }
    
    /**
     * Check if this signature was made by a staff member
     */
    public boolean isStaffSignature() {
        return signerType == SignerType.STAFF || signerType == SignerType.MANAGER;
    }
    
    /**
     * Check if this signature was made by a customer
     */
    public boolean isCustomerSignature() {
        return signerType == SignerType.CUSTOMER;
    }
} 