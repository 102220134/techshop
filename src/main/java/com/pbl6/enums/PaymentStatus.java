package com.pbl6.enums;

import lombok.Getter;

@Getter
public enum PaymentStatus {

    PENDING("pending", "Chờ"),
    PROCESSING("processing","Đang tiến hành"),
    PAID("paid", "đã thanh toán"),
    FAILED("failed", "Thất bại"),
    CANCELED("cancel","Huỷ"),
    REFUNDED("refunded", "Hoàn tiền");

    private final String code;
    private final String label;

    PaymentStatus(String code, String label) {
        this.code = code;
        this.label = label;
    }
}
