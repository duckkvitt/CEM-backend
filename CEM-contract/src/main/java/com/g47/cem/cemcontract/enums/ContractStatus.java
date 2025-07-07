package com.g47.cem.cemcontract.enums;

/**
 * Enum representing the status of a contract
 */
public enum ContractStatus {
    /**
     * Contract is being created or edited, not yet submitted for signing.
     */
    DRAFT("Bản nháp"),

    /**
     * Contract has been generated and is waiting for the seller's (e.g., Manager) signature.
     */
    PENDING_SELLER_SIGNATURE("Chờ bên bán ký"),

    /**
     * Contract has been signed by the seller and is waiting for the customer's signature.
     */
    PENDING_CUSTOMER_SIGNATURE("Chờ khách hàng ký"),

    /**
     * Contract has been fully signed by all parties and is now legally active.
     */
    ACTIVE("Đã ký, có hiệu lực"),

    /**
     * The customer or an internal party has rejected the contract.
     */
    REJECTED("Đã từ chối"),

    /**
     * Contract has been administratively cancelled.
     */
    CANCELLED("Đã hủy"),

    /**
     * Contract has passed its end date and is no longer active.
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
     * Check if the contract is in a state where it is considered signed and active.
     * @return true if the contract is active.
     */
    public boolean isSignedAndActive() {
        return this == ACTIVE;
    }
    
    /**
     * Checks if the contract is in a terminal state (cannot be changed further).
     * @return true if the contract status is final.
     */
    public boolean isTerminal() {
        return this == ACTIVE || this == REJECTED || this == CANCELLED || this == EXPIRED;
    }
} 