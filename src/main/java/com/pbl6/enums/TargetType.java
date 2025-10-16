package com.pbl6.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum TargetType {

    PRODUCT("amount", "Sản phẩm"),
    GLOBAL("global","Toàn hệ thống ");
    @JsonValue
    private final String code;
    private final String label;

    TargetType(String code, String label) {
        this.code = code;
        this.label = label;
    }
}