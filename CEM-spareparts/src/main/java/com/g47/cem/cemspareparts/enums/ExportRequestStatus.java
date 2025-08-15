package com.g47.cem.cemspareparts.enums;

/**
 * Enum representing the status of export requests
 */
public enum ExportRequestStatus {
    PENDING("Pending Review", "Đang chờ duyệt"),
    APPROVED("Approved", "Đã duyệt"),
    REJECTED("Rejected", "Đã từ chối"),
    ISSUED("Issued", "Đã xuất"),
    CANCELLED("Cancelled", "Đã hủy");

    private final String englishName;
    private final String vietnameseName;

    ExportRequestStatus(String englishName, String vietnameseName) {
        this.englishName = englishName;
        this.vietnameseName = vietnameseName;
    }

    public String getEnglishName() {
        return englishName;
    }

    public String getVietnameseName() {
        return vietnameseName;
    }

    public boolean isActive() {
        return this == PENDING || this == APPROVED;
    }

    public boolean isFinal() {
        return this == ISSUED || this == REJECTED || this == CANCELLED;
    }
}
