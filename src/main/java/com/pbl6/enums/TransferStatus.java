package com.pbl6.enums;

import lombok.Getter;

@Getter
public enum TransferStatus {
    PENDING("pending", "Chờ xử lý");

    private final String code;
    private final String label;

    TransferStatus(String code, String label) {
        this.code = code;
        this.label = label;
    }
}
