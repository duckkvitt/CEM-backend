package com.g47.cem.cemcontract.enums;

/**
 * Enum representing the status of a contract
 */
public enum ContractStatus {
    /**
     * Contract has been created but not yet signed
     */
    UNSIGNED("Chưa ký"),
    
    /**
     * Contract has been signed on paper but not digitally confirmed
     */
    PAPER_SIGNED("Đã ký giấy"),
    
    /**
     * Contract has been digitally signed and confirmed
     */
    DIGITALLY_SIGNED("Đã ký điện tử"),
    
    /**
     * Contract has been cancelled or voided
     */
    CANCELLED("Đã hủy"),
    
    /**
     * Contract is expired
     */
    EXPIRED("Đã hết hạn");
    
    private final String vietnameseName;
    
    ContractStatus(String vietnameseName) {
        this.vietnameseName = vietnameseName;
    }
    
    public String getVietnameseName() {
        return vietnameseName;
    }
    
    /**
     * Check if the contract is in a signed state
     */
    public boolean isSigned() {
        return this == PAPER_SIGNED || this == DIGITALLY_SIGNED;
    }
    
    /**
     * Check if the contract is active (signed and not cancelled/expired)
     */
    public boolean isActive() {
        return isSigned() && this != CANCELLED && this != EXPIRED;
    }
} 