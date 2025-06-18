package com.g47.cem.cemdevice.enums;

/**
 * Enum for device status
 */
public enum DeviceStatus {
    ACTIVE("Active"),
    INACTIVE("Inactive"),
    MAINTENANCE("Maintenance"),
    BROKEN("Broken"),
    DISCONTINUED("Discontinued");
    
    private final String displayName;
    
    DeviceStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
} 