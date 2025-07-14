package com.g47.cem.cemcontract.enums;

/**
 * Enum for digital signature status
 */
public enum SignatureStatus {
    /**
     * Signature is valid and verified
     */
    VALID,
    
    /**
     * Signature is invalid (verification failed)
     */
    INVALID,
    
    /**
     * Signature verification is pending
     */
    PENDING_VERIFICATION,
    
    /**
     * Signature could not be verified (unknown error)
     */
    VERIFICATION_FAILED,
    
    /**
     * Certificate used for signature is revoked
     */
    CERTIFICATE_REVOKED,
    
    /**
     * Certificate used for signature is expired
     */
    CERTIFICATE_EXPIRED,
    
    /**
     * Certificate chain could not be verified
     */
    CERTIFICATE_INVALID,
    
    /**
     * Timestamp verification failed
     */
    TIMESTAMP_INVALID,
    
    /**
     * Document was modified after signing
     */
    DOCUMENT_MODIFIED,
    
    /**
     * Signature was corrupted
     */
    CORRUPTED
} 