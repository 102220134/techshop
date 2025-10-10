package com.pbl6.services.strategy;

import com.pbl6.dtos.request.checkout.PaymentRequest;
import com.pbl6.dtos.response.payment.PaymentInitResponse;

public interface PaymentStrategy {
    PaymentInitResponse initiate(PaymentRequest req);
}
