package com.pbl6.enums;

import lombok.Getter;

@Getter
public enum InventoryLocationType {
    WAREHOUSE("warehouse"),
    STORE("store");

    private final String code;

    InventoryLocationType(String code) {
        this.code = code;
    }
}
