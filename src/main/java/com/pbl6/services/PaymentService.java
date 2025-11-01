package com.pbl6.services;

import com.pbl6.dtos.request.webhook.SePayWebhookPayload;
import com.pbl6.dtos.request.checkout.PaymentRequest;
import com.pbl6.dtos.response.payment.PaymentInitResponse;
import com.pbl6.entities.OrderEntity;

public interface PaymentService {
    String handleSePayWebhook(SePayWebhookPayload payload);
    PaymentInitResponse create(OrderEntity order);
}
