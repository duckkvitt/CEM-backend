package com.g47.cem.cemspareparts.enums;

/**
 * Enum representing the status of import requests
 */
public enum ImportRequestStatus {
    PENDING("Pending Review", "Đang chờ duyệt"),
    APPROVED("Approved", "Đã duyệt"),
    REJECTED("Rejected", "Đã từ chối"),
    COMPLETED("Completed", "Đã hoàn thành"),
    CANCELLED("Cancelled", "Đã hủy");

    private final String englishName;
    private final String vietnameseName;

    ImportRequestStatus(String englishName, String vietnameseName) {
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
        return this == COMPLETED || this == REJECTED || this == CANCELLED;
    }
}
