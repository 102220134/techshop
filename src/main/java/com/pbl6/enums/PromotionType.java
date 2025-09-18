package com.pbl6.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

@Getter
public enum PromotionType {

    PERCENTAGE("percentage", "Phần trăm"),
    AMOUNT("amount", "Giảm thẳng");

    private final String code;
    private final String label;

    PromotionType(String code, String label) {
        this.code = code;
        this.label = label;
    }
    public static Optional<PromotionType> fromCode(String code) {
        return Arrays.stream(values())
                .filter(type -> type.getCode().equalsIgnoreCase(code))
                .findFirst();
    }
}
