package com.g47.cem.cemdevice.enums;

/**
 * Enum for customer device status
 */
public enum CustomerDeviceStatus {
    ACTIVE("Active"),
    ERROR("Error"),
    WARRANTY("Under Warranty"),
    EXPIRED("Expired"),
    RETURNED("Returned"),
    REPLACED("Replaced");
    
    private final String displayName;
    
    CustomerDeviceStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
} 