package com.pbl6.services.strategy;



import com.pbl6.dtos.request.checkout.PaymentRequest;
import com.pbl6.dtos.response.payment.BankTransferInfo;
import com.pbl6.dtos.response.payment.PaymentInitResponse;
import com.pbl6.entities.OrderEntity;
import com.pbl6.entities.PaymentEntity;
import com.pbl6.enums.PaymentMethod;
import com.pbl6.enums.PaymentStatus;
import com.pbl6.repositories.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Time;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class BankTransferPayment implements PaymentStrategy {
    private final PaymentRepository paymentRepo;

    @Value("${payment.bankTransfer.webhookSecret}")
    private String webhookSecret;

    @Override
    public PaymentInitResponse initiate(OrderEntity order) {
        PaymentEntity p = PaymentEntity.builder()
                .order(order)
                .amount(order.getTotalAmount())
                .method(PaymentMethod.BANK)
                .status(PaymentStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .provider("MBBank")
                .build();
        p = paymentRepo.save(p);

        String bankNumber = "0976912051";
        String bankName = "MBBank";
        BigDecimal amount = order.getTotalAmount();
        String content = "PY1 " + order.getId();

        String encodedContent = URLEncoder.encode(content, StandardCharsets.UTF_8);

        String qrUrl = String.format(
                "https://qr.sepay.vn/img?acc=%s&bank=%s&amount=%s&des=%s",
                bankNumber,
                bankName,
                amount.stripTrailingZeros().toPlainString(),
                encodedContent
        );

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
                .message("Quét mã QR hoặc chuyển khoản theo thông tin trên")
                .paymentInfo(BankTransferInfo.builder()
                        .type(order.getPaymentMethod())
                        .label(order.getPaymentMethod().getLabel())
                        .bankAccountNumber(bankNumber)
                        .bankAccountName("Lê Anh Vũ")
                        .bankName(bankName)
                        .transferContent(content)
                        .qrCodeUrl(qrUrl)
                        .lifeTime(LocalTime.of(0,5,0))
                        .build())
                .build();
    }
}

