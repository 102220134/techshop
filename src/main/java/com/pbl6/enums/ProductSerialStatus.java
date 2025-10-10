package com.pbl6.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum ProductSerialStatus {
    IN_STOCK("in_stock", "Trong kho"),
    TRANSFER("transfer", "Đang vận chuyển nội bộ"),
    RESERVED("reserved", "Giữ hàng");

    @JsonValue
    private final String code;
    private final String label;

    ProductSerialStatus(String code, String label) {
        this.code = code;
        this.label = label;
    }
}
