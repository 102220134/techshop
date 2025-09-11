package com.pbl6.enums;

import lombok.Getter;

@Getter
public enum DebtStatus {

    UNPAID("unpaid", "Chưa trả"),
    PARTIAL("partial", "Trả một phần"),
    PAID("paid", "Đã trả"),
    CANCELLED("cancelled", "Huỷ");

    private final String code;
    private final String label;

    DebtStatus(String code, String label) {
        this.code = code;
        this.label = label;
    }
}
