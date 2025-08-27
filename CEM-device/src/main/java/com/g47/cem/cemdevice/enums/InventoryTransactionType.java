package com.g47.cem.cemdevice.enums;

/**
 * Enum representing the types of inventory transactions
 */
public enum InventoryTransactionType {
    IMPORT,     // Stock in - from supplier
    EXPORT,     // Stock out - to customer/task
    ADJUSTMENT, // Stock adjustment - corrections, damages, etc.
    TRANSFER,   // Stock transfer between locations
    RETURN      // Return from customer
}


