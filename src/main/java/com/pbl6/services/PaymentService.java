package com.pbl6.services;

import com.pbl6.dtos.request.webhook.SePayWebhookPayload;
import com.pbl6.dtos.response.order.PaymentDetailDto;
import com.pbl6.dtos.response.payment.PaymentInitResponse;
import com.pbl6.entities.OrderEntity;
import com.pbl6.enums.PaymentMethod;

import java.math.BigDecimal;
import java.util.List;

public interface PaymentService {
    String handleSePayWebhook(SePayWebhookPayload payload);
    PaymentInitResponse create(OrderEntity order);

    void createTransaction(OrderEntity order, BigDecimal remainingAmount, PaymentMethod paymentMethod);

    List<PaymentDetailDto> getPaymentsByOrderId(Long orderId);
}
