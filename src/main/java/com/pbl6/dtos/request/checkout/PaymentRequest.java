package com.pbl6.dtos.request.checkout;

import com.pbl6.enums.PaymentMethod;
import com.pbl6.enums.ReceiveMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class PaymentRequest {
    Long orderId;
    BigDecimal grossAmount;
    BigDecimal directDiscount;
    BigDecimal voucherDiscount;
    BigDecimal totalAmount;
    PaymentMethod paymentMethod;
    ReceiveMethod receiveMethod;
}
