package com.g47.cem.cemspareparts.enums;

/**
 * Enum representing types of inventory transactions
 */
public enum InventoryTransactionType {
    IMPORT("Import", "Nhập kho"),
    EXPORT("Export", "Xuất kho"),
    ADJUSTMENT("Adjustment", "Điều chỉnh"),
    TRANSFER("Transfer", "Chuyển kho");

    private final String englishName;
    private final String vietnameseName;

    InventoryTransactionType(String englishName, String vietnameseName) {
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
