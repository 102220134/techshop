package com.pbl6.enums;

import lombok.Getter;

@Getter
public enum DiscountType {

    AMOUNT("amount", "Giảm giá tiền"),
    PERCENT("percent", "Giảm theo phần trăm");

    private final String code;
    private final String label;

    DiscountType(String code, String label) {
        this.code = code;
        this.label = label;
    }
}