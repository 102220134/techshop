package com.pbl6.enums;

import lombok.Getter;

@Getter
public enum PaymentMethod {

    COD("cod", "Thanh toán khi nhận hàng"),
    BANK_TRANSFER("bank_transfer", "Chuyển khoản"),
    CREDIT_CARD("credit_card", "Thẻ tín dụng"),
    E_WALLET("e_wallet", "Ví điện tử");

    private final String code;
    private final String label;

    PaymentMethod(String code, String label) {
        this.code = code;
        this.label = label;
    }
}
