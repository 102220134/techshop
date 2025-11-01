package com.pbl6.services.strategy;

import com.pbl6.dtos.request.checkout.PaymentRequest;
import com.pbl6.dtos.response.payment.PaymentInitResponse;
import com.pbl6.entities.OrderEntity;

public interface PaymentStrategy {
    PaymentInitResponse initiate(OrderEntity order);
}
