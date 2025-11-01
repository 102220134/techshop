package com.pbl6.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.pbl6.exceptions.AppException;
import com.pbl6.exceptions.ErrorCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public enum PaymentMethod {
    COD( "Thanh toán khi nhận hàng"),
    BANK( "Chuyển khoản"),
    VNPAY("Ví điện tử");

    private final String label;

    PaymentMethod( String label) {
        this.label = label;
    }
}
