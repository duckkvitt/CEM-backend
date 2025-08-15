package com.g47.cem.cemspareparts.enums;

/**
 * Enum representing approval status for requests
 */
public enum ApprovalStatus {
    APPROVED("Approved", "Đã duyệt"),
    REJECTED("Rejected", "Đã từ chối");

    private final String englishName;
    private final String vietnameseName;

    ApprovalStatus(String englishName, String vietnameseName) {
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
