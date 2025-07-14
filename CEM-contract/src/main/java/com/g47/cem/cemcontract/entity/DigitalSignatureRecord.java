package com.g47.cem.cemcontract.entity;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.g47.cem.cemcontract.enums.SignatureAlgorithm;
import com.g47.cem.cemcontract.enums.SignatureStatus;
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
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DigitalSignatureRecord entity for PAdES-compliant digital signatures
 */
@Entity
@Table(name = "digital_signature_records")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class DigitalSignatureRecord {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "certificate_id")
    private DigitalCertificate certificate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "signer_type", nullable = false, length = 50)
    private SignerType signerType;
    
    @Column(name = "signer_id")
    private Long signerId;
    
    @Column(name = "signer_name", nullable = false, length = 255)
    private String signerName;
    
    @Column(name = "signer_email", nullable = false, length = 255)
    private String signerEmail;
    
    @Column(name = "signature_field_name", length = 100)
    private String signatureFieldName; // PDF signature field name
    
    @Enumerated(EnumType.STRING)
    @Column(name = "signature_algorithm", nullable = false, length = 50)
    private SignatureAlgorithm signatureAlgorithm; // SHA256_WITH_RSA, SHA256_WITH_ECDSA, etc.
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    @Builder.Default
    private SignatureStatus status = SignatureStatus.VALID;
    
    // Signature appearance data (canvas drawing)
    @Lob
    @Column(name = "signature_image_data")
    private byte[] signatureImageData; // PNG image from canvas
    
    @Column(name = "signature_image_width")
    private Integer signatureImageWidth;
    
    @Column(name = "signature_image_height")
    private Integer signatureImageHeight;
    
    // PDF signature position
    @Column(name = "page_number")
    private Integer pageNumber;
    
    @Column(name = "signature_x")
    private Float signatureX;
    
    @Column(name = "signature_y")
    private Float signatureY;
    
    @Column(name = "signature_width")
    private Float signatureWidth;
    
    @Column(name = "signature_height")
    private Float signatureHeight;
    
    // Cryptographic signature data
    @Lob
    @Column(name = "signature_value")
    private byte[] signatureValue; // Digital signature bytes
    
    @Column(name = "signature_hash", length = 128)
    private String signatureHash; // Hash of signed data
    
    @Column(name = "hash_algorithm", length = 50)
    private String hashAlgorithm; // SHA-256, SHA-512, etc.
    
    // Timestamp information
    @Column(name = "timestamp_url", length = 500)
    private String timestampUrl; // TSA URL if timestamped
    
    @Lob
    @Column(name = "timestamp_token")
    private byte[] timestampToken; // RFC 3161 timestamp token
    
    @Column(name = "timestamp_verified")
    @Builder.Default
    private Boolean timestampVerified = false;
    
    // Signature verification info
    @Column(name = "signature_verified")
    @Builder.Default
    private Boolean signatureVerified = false;
    
    @Column(name = "certificate_verified")
    @Builder.Default
    private Boolean certificateVerified = false;
    
    @Column(name = "verification_errors", columnDefinition = "TEXT")
    private String verificationErrors; // JSON array of verification errors
    
    @Column(name = "last_verified_at")
    private LocalDateTime lastVerifiedAt;
    
    // Signature metadata
    @Column(name = "reason", length = 500)
    private String reason; // Reason for signing
    
    @Column(name = "location", length = 255)
    private String location; // Geographic location
    
    @Column(name = "contact_info", length = 255)
    private String contactInfo; // Contact information
    
    // Audit trail
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    
    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;
    
    @Column(name = "signed_at", nullable = false)
    @Builder.Default
    private LocalDateTime signedAt = LocalDateTime.now();
    
    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    // Helper methods
    
    /**
     * Check if this signature is currently valid
     */
    public boolean isValid() {
        return status == SignatureStatus.VALID && 
               signatureVerified && 
               certificateVerified;
    }
    
    /**
     * Check if signature has timestamp
     */
    public boolean hasTimestamp() {
        return timestampToken != null && timestampToken.length > 0;
    }
    
    /**
     * Check if signature is timestamped and verified
     */
    public boolean isTimestampVerified() {
        return hasTimestamp() && timestampVerified;
    }
    
    /**
     * Get complete verification status
     */
    public boolean isCompletelyVerified() {
        return isValid() && (timestampToken == null || timestampVerified);
    }
} 