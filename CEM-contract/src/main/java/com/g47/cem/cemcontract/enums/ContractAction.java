package com.g47.cem.cemcontract.enums;

/**
 * Enum representing actions performed on contracts for audit trail
 */
public enum ContractAction {
    /**
     * Contract was created
     */
    CREATED("Tạo mới"),
    
    /**
     * Contract information was updated
     */
    UPDATED("Cập nhật"),

    /**
     * Contract status was changed manually
     */
    STATUS_CHANGED("Thay đổi trạng thái"),
    
    /**
     * Contract was signed
     */
    SIGNED("Ký"),
    
    /**
     * Contract was hidden from view
     */
    HIDDEN("Ẩn"),
    
    /**
     * Contract was restored from hidden state
     */
    RESTORED("Khôi phục"),
    
    /**
     * Contract was cancelled
     */
    CANCELLED("Hủy"),
    
    /**
     * Contract file was uploaded
     */
    FILE_UPLOADED("Tải file lên"),
    
    /**
     * Digital signature was added
     */
    DIGITAL_SIGNATURE_ADDED("Thêm chữ ký số");
    
    private final String vietnameseName;
    
    ContractAction(String vietnameseName) {
        this.vietnameseName = vietnameseName;
    }
    
    public String getVietnameseName() {
        return vietnameseName;
    }
} 