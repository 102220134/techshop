package com.pbl6.enums;

import lombok.Getter;

@Getter
public enum OrderStatus {

    PENDING("pending","Chờ xử lý"),
    CONFIRMED("confirmed","Đã xác nhận"),
    PAID("paid","Đã thanh toán"),
    SHIPPED("shipped","Đang giao"),
    DELIVERED("delivered","Đã giao"),
    COMPLETED("completed","Hoàn tất"),
    CANCELLED("cancelled","Hủy"),
    REFUSED("refused","Từ chối"),
    RETURNED("returned","Trả hàng");

    private final String code;
    private final String label;

    OrderStatus(String code, String label) {
        this.code = code;
        this.label = label;
    }
}
