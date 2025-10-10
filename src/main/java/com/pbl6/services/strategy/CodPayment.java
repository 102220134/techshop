package com.pbl6.services.strategy;

import com.pbl6.dtos.request.checkout.PaymentRequest;
import com.pbl6.dtos.response.payment.CodInfo;
import com.pbl6.dtos.response.payment.PaymentInitResponse;
import com.pbl6.enums.DebtStatus;
import com.pbl6.enums.OrderStatus;
import com.pbl6.enums.PaymentMethod;
import com.pbl6.repositories.PaymentRepository;
import com.pbl6.services.DebtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CodPayment implements PaymentStrategy {
    private final PaymentRepository paymentRepo;
    private final DebtService debtService;

    @Override
    public PaymentInitResponse initiate(PaymentRequest req) {
        debtService.createOrUpdate(req, DebtStatus.UNPAID);
        return PaymentInitResponse.builder()
                .orderId(req.getOrderId())
                .amount(req.getTotalAmount())
                .message("Đơn hàng của bạn đang được xử lý")
                .paymentInfo(CodInfo.builder()
                        .type(PaymentMethod.COD.getCode())
                        .label(PaymentMethod.COD.getLabel())
                        .build())
                .build();
    }
}

