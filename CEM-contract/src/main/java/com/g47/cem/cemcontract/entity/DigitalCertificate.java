package com.g47.cem.cemcontract.entity;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.g47.cem.cemcontract.enums.CertificateStatus;
import com.g47.cem.cemcontract.enums.CertificateType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DigitalCertificate entity for managing digital certificates
 */
@Entity
@Table(name = "digital_certificates")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class DigitalCertificate {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "alias", nullable = false, length = 255)
    private String alias;
    
    @Column(name = "subject_dn", nullable = false, columnDefinition = "TEXT")
    private String subjectDN; // Distinguished Name of certificate subject
    
    @Column(name = "issuer_dn", nullable = false, columnDefinition = "TEXT")
    private String issuerDN; // Distinguished Name of certificate issuer
    
    @Column(name = "serial_number", nullable = false, length = 100)
    private String serialNumber;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "certificate_type", nullable = false, length = 50)
    private CertificateType certificateType; // SELF_SIGNED, CA_ISSUED, ORGANIZATION
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    @Builder.Default
    private CertificateStatus status = CertificateStatus.ACTIVE;
    
    @Column(name = "valid_from", nullable = false)
    private LocalDateTime validFrom;
    
    @Column(name = "valid_to", nullable = false)
    private LocalDateTime validTo;
    
    @Column(name = "certificate_data", nullable = false)
    private byte[] certificateData; // X.509 certificate binary data
    
    @Column(name = "private_key_data", columnDefinition = "bytea")
    private byte[] privateKeyData; // PKCS12 keystore containing both certificate and private key
    
    @Column(name = "public_key_data", nullable = false, columnDefinition = "bytea")
    private byte[] publicKeyData; // Encoded public key data
    
    @Column(name = "key_algorithm", length = 50)
    private String keyAlgorithm; // RSA, ECDSA, etc.
    
    @Column(name = "key_size")
    private Integer keySize; // Key size in bits
    
    @Column(name = "signature_algorithm", length = 100)
    private String signatureAlgorithm; // SHA256withRSA, SHA256withECDSA, etc.
    
    @Column(name = "fingerprint_sha1", length = 64)
    private String fingerprintSha1;
    
    @Column(name = "fingerprint_sha256", length = 64)
    private String fingerprintSha256;
    
    @Column(name = "created_by", nullable = false, length = 255)
    private String createdBy;
    
    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    // Helper methods
    
    /**
     * Check if certificate is currently valid
     */
    public boolean isCurrentlyValid() {
        LocalDateTime now = LocalDateTime.now();
        return status == CertificateStatus.ACTIVE && 
               !now.isBefore(validFrom) && 
               !now.isAfter(validTo);
    }
    
    /**
     * Check if certificate is expired
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(validTo);
    }
    
    /**
     * Get certificate subject common name
     */
    public String getCommonName() {
        if (subjectDN != null && subjectDN.contains("CN=")) {
            String cn = subjectDN.substring(subjectDN.indexOf("CN=") + 3);
            if (cn.contains(",")) {
                cn = cn.substring(0, cn.indexOf(","));
            }
            return cn.trim();
        }
        return null;
    }
} 