package com.pbl6.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.pbl6.exceptions.AppException;
import com.pbl6.exceptions.ErrorCode;
import lombok.Getter;

@Getter
public enum ReceiveMethod {
    PICKUP( "Nhận tại cửa hàng"),
    DELIVERY("Giao hàng tận nơi");

    private final String label;

    ReceiveMethod(String label) {
        this.label = label;
    }
}
