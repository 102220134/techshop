package com.pbl6.services.strategy;

import com.pbl6.dtos.request.checkout.PaymentRequest;
import com.pbl6.dtos.response.payment.CodInfo;
import com.pbl6.dtos.response.payment.PaymentInitResponse;
import com.pbl6.entities.OrderEntity;
import com.pbl6.enums.DebtStatus;
import com.pbl6.enums.OrderStatus;
import com.pbl6.enums.PaymentMethod;
import com.pbl6.repositories.PaymentRepository;
import com.pbl6.services.DebtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CodPayment implements PaymentStrategy {
    private final PaymentRepository paymentRepo;
    private final DebtService debtService;

    @Override
    public PaymentInitResponse initiate(OrderEntity order) {
        debtService.create(order);
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
                .message("Đơn hàng của bạn đang được xử lý")
                .paymentInfo(CodInfo.builder()
                        .type(PaymentMethod.COD)
                        .label(PaymentMethod.COD.getLabel())
                        .build())
                .build();
    }
}

