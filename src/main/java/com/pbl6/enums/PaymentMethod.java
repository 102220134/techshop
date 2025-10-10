package com.pbl6.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.pbl6.exceptions.AppException;
import com.pbl6.exceptions.ErrorCode;
import lombok.Getter;

@Getter
public enum PaymentMethod {

    COD("cod", "Thanh toán khi nhận hàng"),
    BANK("bank", "Chuyển khoản"),
    VNPAY("vnpay", "Ví điện tử");

    @JsonValue
    private final String code;
    private final String label;

    PaymentMethod(String code, String label) {
        this.code = code;
        this.label = label;
    }
}
