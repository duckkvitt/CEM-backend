package com.g47.cem.cemdevice.enums;

/**
 * Enum representing reference types for inventory transactions
 */
public enum InventoryReferenceType {
    IMPORT_REQUEST("Import Request", "Phiếu nhập"),
    CONTRACT("Contract", "Hợp đồng"),
    ADJUSTMENT("Adjustment", "Điều chỉnh"),
    TRANSFER("Transfer", "Chuyển kho");

    private final String englishName;
    private final String vietnameseName;

    InventoryReferenceType(String englishName, String vietnameseName) {
        this.englishName = englishName;
        this.vietnameseName = vietnameseName;
    }

    public String getEnglishName() {
        return englishName;
    }

    public String getVietnameseName() {
        return vietnameseName;
    }
}
