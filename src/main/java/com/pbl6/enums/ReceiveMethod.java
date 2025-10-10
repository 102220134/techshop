package com.pbl6.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.pbl6.exceptions.AppException;
import com.pbl6.exceptions.ErrorCode;
import lombok.Getter;

@Getter
public enum ReceiveMethod {
    RECEIVE_AT_STORE("receive_at_store", "Nhận tại cửa hàng"),
    SHIPMENT("shipment","Giao hàng tận nơi");

    @JsonValue
    private final String code;
    private final String label;

    ReceiveMethod(String code, String label) {
        this.code = code;
        this.label = label;
    }
}
