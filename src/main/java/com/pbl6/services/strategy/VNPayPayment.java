package com.pbl6.services.strategy;

import com.pbl6.dtos.request.checkout.PaymentRequest;
import com.pbl6.dtos.response.payment.PaymentInitResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VNPayPayment  implements PaymentStrategy {
    @Override
    public PaymentInitResponse initiate(PaymentRequest req) {

        return PaymentInitResponse.builder()
                .orderId(req.getOrderId())
                .amount(req.getTotalAmount())
                .message("Đang làm")
                .build();
    }
}
