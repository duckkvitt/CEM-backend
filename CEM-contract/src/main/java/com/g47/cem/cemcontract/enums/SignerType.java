package com.g47.cem.cemcontract.enums;

/**
 * Enum representing the type of signer
 */
public enum SignerType {
    /**
     * Staff member signing the contract
     */
    STAFF("Nhân viên"),
    
    /**
     * Customer signing the contract
     */
    CUSTOMER("Khách hàng"),
    
    /**
     * Manager approving the contract
     */
    MANAGER("Quản lý");
    
    private final String vietnameseName;
    
    SignerType(String vietnameseName) {
        this.vietnameseName = vietnameseName;
    }
    
    public String getVietnameseName() {
        return vietnameseName;
    }
} 