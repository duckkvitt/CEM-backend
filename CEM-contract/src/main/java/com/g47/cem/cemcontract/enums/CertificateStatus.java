package com.g47.cem.cemcontract.enums;

/**
 * Enum for certificate status
 */
public enum CertificateStatus {
    /**
     * Certificate is active and can be used for signing
     */
    ACTIVE,
    
    /**
     * Certificate is inactive/disabled
     */
    INACTIVE,
    
    /**
     * Certificate has been revoked
     */
    REVOKED,
    
    /**
     * Certificate has expired
     */
    EXPIRED,
    
    /**
     * Certificate is pending activation
     */
    PENDING,
    
    /**
     * Certificate is suspended temporarily
     */
    SUSPENDED
} 