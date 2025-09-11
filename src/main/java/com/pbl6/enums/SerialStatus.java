package com.pbl6.enums;

import lombok.Getter;

@Getter
public enum SerialStatus {

    IN_STOCK("in_stock", "Trong kho"),
    RESERVED("reserved", "Đã giữ chỗ"),
    SOLD("sold", "Đã bán"),
    RETURNED("returned", "Đã trả");

    private final String code;
    private final String label;

    SerialStatus(String code, String label) {
        this.code = code;
        this.label = label;
    }
}
