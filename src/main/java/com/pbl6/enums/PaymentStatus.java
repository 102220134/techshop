package com.pbl6.enums;

import lombok.Getter;

@Getter
public enum PaymentStatus {

    PENDING("pending", "Chờ"),
    COMPLETED("completed", "Thành công"),
    FAILED("failed", "Thất bại"),
    REFUNDED("refunded", "Hoàn tiền");

    private final String code;
    private final String label;

    PaymentStatus(String code, String label) {
        this.code = code;
        this.label = label;
    }
}
