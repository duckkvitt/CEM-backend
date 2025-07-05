package com.g47.cem.cemdevice.enums;

import lombok.Getter;

/**
 * Enum for customer device status
 */
@Getter
public enum CustomerDeviceStatus {
    ACTIVE("Active"),
    INACTIVE("Inactive"),
    ERROR("Error"),
    WARRANTY("Warranty"),
    EXPIRED("Expired");

    private final String displayName;

    CustomerDeviceStatus(String displayName) {
        this.displayName = displayName;
    }
} 