package com.pbl6.dtos.response.payment;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.pbl6.enums.PaymentMethod;
import com.pbl6.enums.ReceiveMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO trả về cho FE sau khi khởi tạo thanh toán (checkout)
 * Dùng chung cho mọi phương thức: banking / ví / COD / trả góp
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentInitResponse {
    Long orderId;
    BigDecimal grossAmount;
    BigDecimal directDiscount;
    BigDecimal voucherDiscount;
    BigDecimal totalAmount;
    PaymentMethod paymentMethod;
    ReceiveMethod receiveMethod;
    PaymentInfo paymentInfo;
    private String message;

}
