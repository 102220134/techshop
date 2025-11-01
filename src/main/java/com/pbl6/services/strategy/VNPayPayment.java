package com.pbl6.services.strategy;

import com.pbl6.dtos.request.checkout.PaymentRequest;
import com.pbl6.dtos.response.payment.PaymentInitResponse;
import com.pbl6.entities.OrderEntity;
import lombok.RequiredArgsConstructor;
import org.hibernate.query.Order;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class VNPayPayment  implements PaymentStrategy {
    @Override
    public PaymentInitResponse initiate(OrderEntity order) {

        return PaymentInitResponse.builder()
                .orderId(order.getId())
                .grossAmount(order.getOrderItems().stream()
                        .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                        .reduce(BigDecimal.ZERO, BigDecimal::add))
                .directDiscount(order.getOrderItems().stream()
                        .map(item -> item.getDiscountAmount().multiply(BigDecimal.valueOf(item.getQuantity())))
                        .reduce(BigDecimal.ZERO, BigDecimal::add))
                .voucherDiscount(order.getVoucherDiscount())
                .totalAmount(order.getTotalAmount())
                .message("Đang làm")
                .build();
    }
}
