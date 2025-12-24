package com.pbl6.services.impl;

import com.pbl6.dtos.request.webhook.SePayWebhookPayload;
import com.pbl6.dtos.response.order.PaymentDetailDto;
import com.pbl6.dtos.response.order.PaymentDto;
import com.pbl6.dtos.response.payment.PaymentInitResponse;
import com.pbl6.entities.OrderEntity;
import com.pbl6.entities.PaymentEntity;
import com.pbl6.enums.PaymentMethod;
import com.pbl6.enums.PaymentStatus;
import com.pbl6.exceptions.AppException;
import com.pbl6.exceptions.ErrorCode;
import com.pbl6.repositories.OrderRepository;
import com.pbl6.repositories.PaymentRepository;
import com.pbl6.services.PaymentService;
import com.pbl6.services.strategy.BankTransferPayment;
import com.pbl6.services.strategy.CodPayment;
import com.pbl6.services.strategy.VNPayPayment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final CodPayment cod;
    private final BankTransferPayment bankTransfer;
    private final VNPayPayment vnPayPayment;

    private final PaymentRepository paymentRepo;
    private final OrderRepository orderRepository;
    private final SimpMessagingTemplate template;

    // Nếu bạn đã có DebtService như bài trước, hãy inject vào đây
    // private final DebtService debtService;

    @Override
    @Transactional
    public String handleSePayWebhook(SePayWebhookPayload payload) {
        // 1️⃣ Validate & Lấy Order ID
        Long orderId = extractOrderId(payload.getContent());
        if (orderId == null) {
            log.error("Cannot extract orderId from content: {}", payload.getContent());
            return "invalid content"; // Trả về text để controller log, ko nên throw 500 cho webhook
        }

        // 2️⃣ Load Order & Payment (Null Safety)
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Order not found"));

        PaymentEntity payment = paymentRepo.findTopByOrderIdOrderByIdDesc(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "No pending payment found for order"));

        // 3️⃣ Idempotency Check (Tránh xử lý trùng)
        if (PaymentStatus.PAID.equals(payment.getStatus())) {
            log.info("Payment {} already completed. Ignoring webhook.", payment.getId());
            return "already completed";
        }

        // 4️⃣ Chuẩn bị dữ liệu so sánh
        BigDecimal expectedAmount = payment.getAmount();
        BigDecimal actualAmount = payload.getTransferAmount();

        // Cập nhật thông tin giao dịch vào Payment trước
        payment.setTransactionRef(payload.getReferenceCode());
        payment.setPaidAt(LocalDateTime.now());

        String resultMessage;

        // 5️⃣ Xử lý Logic so sánh tiền
        // Case A: Đủ tiền hoặc Dư tiền (>=)
        if (actualAmount.compareTo(expectedAmount) >= 0) {
            payment.setStatus(PaymentStatus.PAID);

            // Xử lý Tài chính (Order & Debt)
            updateOrderFinancials(order, actualAmount); // Quan trọng: Update theo số thực tế nhận

            resultMessage = (actualAmount.compareTo(expectedAmount) > 0) ? "overpaid" : "success";
            if (actualAmount.compareTo(expectedAmount) > 0) {
                log.warn("Order {} overpaid! Expected: {}, Actual: {}", orderId, expectedAmount, actualAmount);
            }
        }
        // Case B: Thiếu tiền (<)
        else {
            payment.setStatus(PaymentStatus.FAILED); // Hoặc PARTIAL nếu hệ thống hỗ trợ
            log.warn("Order {} underpaid. Expected: {}, Actual: {}", orderId, expectedAmount, actualAmount);
            resultMessage = "underpaid";

            // Tùy nghiệp vụ: Có thể vẫn cộng tiền vào Order nhưng không mark Payment là PAID hoàn toàn
            // updateOrderFinancials(order, actualAmount);
        }

        // 6️⃣ Save & Notify (DRY - Viết 1 lần)
        paymentRepo.save(payment); // Save payment đã đổi status
        orderRepository.save(order); // Save order đã đổi paidAmount

        // Gửi socket báo client
        template.convertAndSend("/topic/" + orderId, payment.getStatus());

        return resultMessage;
    }

    /**
     * Hàm helper để cập nhật tài chính Order (Đồng bộ với logic Debt 1-1 ở bài trước)
     */
    private void updateOrderFinancials(OrderEntity order, BigDecimal incomingAmount) {
        // Null safety
        BigDecimal currentPaid = order.getPaidAmount() == null ? BigDecimal.ZERO : order.getPaidAmount();

        // Cộng dồn tiền đã trả
        order.setPaidAmount(currentPaid.add(incomingAmount));

        // Tính lại tiền còn thiếu
        order.setRemainingAmount(order.getTotalAmount().subtract(order.getPaidAmount()));

        // --- TÍCH HỢP DEBT (Nếu có) ---
        // if (debtService != null) {
        //     debtService.allocatePaymentToDebt(order, incomingAmount);
        // }
    }

    @Transactional
    @Override
    public PaymentInitResponse create(OrderEntity order) {
        PaymentMethod m = order.getPaymentMethod();
        if (m == null) throw new AppException(ErrorCode.VALIDATION_ERROR, "Payment method is null");

        return switch (m) {
            case COD -> cod.initiate(order);
            case BANK -> bankTransfer.initiate(order);
            case VNPAY -> vnPayPayment.initiate(order);
            default -> throw new AppException(ErrorCode.VALIDATION_ERROR, "Unsupported payment method");
        };
    }
    @Transactional
    @Override
    public void createTransaction(OrderEntity order, BigDecimal remainingAmount, PaymentMethod paymentMethod) {
        PaymentEntity payment = new PaymentEntity();
        payment.setOrder(order);
        payment.setAmount(remainingAmount);
        payment.setMethod(paymentMethod);
        payment.setStatus(PaymentStatus.PAID);
        payment.setCreatedAt(LocalDateTime.now());
        payment.setPaidAt(LocalDateTime.now());
        paymentRepo.save(payment);
    }

    @Override
    public List<PaymentDetailDto> getPaymentsByOrderId(Long orderId) {
        List<PaymentEntity> payments = paymentRepo.findByOrderId(orderId);

        return payments.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    private PaymentDetailDto mapToDto(PaymentEntity entity) {
        return PaymentDetailDto.builder()
                .id(entity.getId())
                .orderId(entity.getOrder().getId())
                .amount(entity.getAmount())
                .method(entity.getMethod())
                .status(entity.getStatus())
                .provider(entity.getProvider())
                .transactionRef(entity.getTransactionRef())
                .paidAt(entity.getPaidAt())
                .createdAt(entity.getCreatedAt())
                .build();
    }
    private Long extractOrderId(String content) {
        if (content == null || content.isBlank()) return null;
        // Regex nên clear hơn, ví dụ tiền tố từ config
        Pattern pattern = Pattern.compile("(?i)PY1\\s*(\\d+)");
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            try {
                return Long.valueOf(matcher.group(1));
            } catch (NumberFormatException e) {
                log.error("Error parsing orderId from content: {}", content);
                return null;
            }
        }
        return null;
    }
}