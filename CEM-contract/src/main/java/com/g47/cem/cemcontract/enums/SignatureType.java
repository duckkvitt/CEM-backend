package com.g47.cem.cemcontract.enums;

/**
 * Enum representing the type of signature used
 */
public enum SignatureType {
    /**
     * Digital signature using electronic signing
     */
    DIGITAL("Ký điện tử"),
    
    /**
     * Physical paper signature
     */
    PAPER("Ký giấy"),
    
    /**
     * Digital image of handwritten signature
     */
    DIGITAL_IMAGE("Ảnh chữ ký");
    
    private final String vietnameseName;
    
    SignatureType(String vietnameseName) {
        this.vietnameseName = vietnameseName;
    }
    
    public String getVietnameseName() {
        return vietnameseName;
    }
} 