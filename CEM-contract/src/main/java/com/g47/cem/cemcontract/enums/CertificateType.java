package com.g47.cem.cemcontract.enums;

/**
 * Enum for different types of digital certificates
 */
public enum CertificateType {
    /**
     * Self-signed certificate for testing/development
     */
    SELF_SIGNED,
    
    /**
     * Certificate issued by a Certificate Authority
     */
    CA_ISSUED,
    
    /**
     * Organization/Enterprise certificate
     */
    ORGANIZATION,
    
    /**
     * Personal/Individual certificate
     */
    PERSONAL,
    
    /**
     * Code signing certificate
     */
    CODE_SIGNING,
    
    /**
     * Document signing certificate
     */
    DOCUMENT_SIGNING
} 