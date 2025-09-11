package com.pbl6.enums;

public enum PurchaseOrderStatus {
    OPEN("open", "Tạo đơn"),
    APPROVED("approved ", "Đã duyệt"),
    RECEIVED("received ", "Đã nhập hàng"),
    CANCELLED("cancelled", "Huỷ");

    private final String code;
    private final String label;

    PurchaseOrderStatus(String code, String label) {
        this.code = code;
        this.label = label;
    }
}
