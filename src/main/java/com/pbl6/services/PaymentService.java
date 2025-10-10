package com.pbl6.services;

import com.pbl6.dtos.request.SePayWebhookPayload;
import com.pbl6.dtos.request.checkout.PaymentRequest;
import com.pbl6.dtos.response.payment.PaymentInitResponse;

public interface PaymentService {
    String handleSePayWebhook(SePayWebhookPayload payload);
    PaymentInitResponse create(PaymentRequest req);
}
