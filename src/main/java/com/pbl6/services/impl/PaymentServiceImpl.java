package com.pbl6.services.impl;

import com.pbl6.dtos.request.webhook.SePayWebhookPayload;
import com.pbl6.dtos.request.checkout.PaymentRequest;
import com.pbl6.dtos.response.payment.PaymentInitResponse;
import com.pbl6.entities.OrderEntity;
import com.pbl6.entities.PaymentEntity;
import com.pbl6.enums.OrderStatus;
import com.pbl6.enums.PaymentMethod;
import com.pbl6.enums.PaymentStatus;
import com.pbl6.repositories.PaymentRepository;
import com.pbl6.services.OrderService;
import com.pbl6.services.PaymentService;
import com.pbl6.services.strategy.BankTransferPayment;
import com.pbl6.services.strategy.CodPayment;
import com.pbl6.services.strategy.VNPayPayment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {
    private final CodPayment cod;
    private final BankTransferPayment bankTransfer;
    private final VNPayPayment vnPayPayment;

    private final PaymentRepository paymentRepo;
    private final OrderService orderService;

    private final SimpMessagingTemplate template;

    @Override
    public String handleSePayWebhook(SePayWebhookPayload payload) {

        // 1️⃣ Tách orderId
        Long orderId = extractOrderId(payload.getContent());
        if (orderId == null) {
            log.warn("Order ID not found in content: {}", payload.getContent());
            return "order id not found in content";
        }

        // 2️⃣ Tìm payment tương ứng
        Optional<PaymentEntity> optPayment = paymentRepo.findTopByOrderIdOrderByIdDesc(orderId);


        PaymentEntity payment = optPayment.get();
        BigDecimal expectedAmount = payment.getAmount();
        BigDecimal actualAmount = payload.getTransferAmount();

        // 3️⃣ Kiểm tra trạng thái hiện tại
        if (PaymentStatus.PAID.equals(payment.getStatus())) {
            log.info("Payment {} already completed, ignoring duplicate webhook", payment.getId());
            return "already completed";
        }

        // 4️⃣ Cập nhật thông tin thanh toán
        payment.setPaidAt(LocalDateTime.now());
        payment.setTransactionRef(payload.getReferenceCode());

        // 5️⃣ So sánh số tiền
        int compare = actualAmount.compareTo(expectedAmount);
        switch (compare) {
            case 0 -> { // ✅ Đúng số tiền
                payment.setStatus(PaymentStatus.PAID);
                paymentRepo.save(payment);
                orderService.confirmOrder(orderId);
                template.convertAndSend("/topic/"+orderId, payment.getStatus());
                log.info("Payment success for order {}, amount={}", orderId, actualAmount);
                return "payment success";
            }
            case -1 -> { // ⚠️ Thiếu tiền
                payment.setStatus(PaymentStatus.FAILED);
                paymentRepo.save(payment);
                log.warn("Underpaid: expected={} actual={}", expectedAmount, actualAmount);
                template.convertAndSend("/topic/"+orderId, payment.getStatus());
                return "underpaid";
            }
            case 1 -> { // ⚠️ Dư tiền
                payment.setStatus(PaymentStatus.FAILED);
                paymentRepo.save(payment);
                log.warn("Overpaid: expected={} actual={}", expectedAmount, actualAmount);
                template.convertAndSend("/topic/"+orderId, payment.getStatus());
                return "overpaid";
            }
            default -> {
                log.error("Unexpected compare result for payment {}", payment.getId());
                return "unexpected amount comparison";
            }
        }
    }

    @Override
    public PaymentInitResponse create(OrderEntity order) {
        PaymentMethod m = order.getPaymentMethod();
        return switch (m) {
            case COD -> cod.initiate(order);
            case BANK -> bankTransfer.initiate(order);
            case VNPAY -> vnPayPayment.initiate(order);
        };
    }

    private Long extractOrderId(String content) {
        if (content == null || content.isBlank()) return null;
        Pattern pattern = Pattern.compile("(?i)PY1\\s*(\\d+)");
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return Long.valueOf(matcher.group(1));
        }
        log.warn("Không tìm thấy orderId trong content: {}", content);
        return null;
    }
}
