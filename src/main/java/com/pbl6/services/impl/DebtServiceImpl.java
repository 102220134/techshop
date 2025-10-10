package com.pbl6.services.impl;

import com.pbl6.dtos.request.checkout.PaymentRequest;
import com.pbl6.entities.DebtEntity;
import com.pbl6.entities.OrderEntity;
import com.pbl6.enums.DebtStatus;
import com.pbl6.repositories.DebtRepository;
import com.pbl6.repositories.OrderRepository;
import com.pbl6.services.DebtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;


@Service
@RequiredArgsConstructor
public class DebtServiceImpl implements DebtService {

    private final DebtRepository debtRepository;
    private final OrderRepository orderRepository;

    @Override
    @Transactional
    public void createOrUpdate(PaymentRequest req, DebtStatus debtStatus) {
        // 1️⃣ Lấy thông tin order
        OrderEntity order = orderRepository.findById(req.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // 2️⃣ Tìm công nợ hiện có
        DebtEntity debt = debtRepository.findByOrderId(order.getId()).orElse(null);

        // 3️⃣ Nếu chưa có -> tạo mới
        if (debt == null) {
            debt = new DebtEntity();
            debt.setOrder(order);
            debt.setUser(order.getUser());
            debt.setTotalAmount(order.getTotalAmount());
            debt.setPaidAmount(req.getTotalAmount()!= null ? req.getTotalAmount() : BigDecimal.ZERO);
            debt.setStatus(debtStatus.getCode());
            debt.setDueDate(LocalDate.now().plusDays(7).atStartOfDay()); // ví dụ cho phép nợ 7 ngày
            debtRepository.save(debt);
            return;
        }

        // 4️⃣ Nếu đã có -> cập nhật paid_amount
        BigDecimal paidAmount = debt.getPaidAmount().add(req.getTotalAmount());
        debt.setPaidAmount(paidAmount);

        // 5️⃣ Xác định trạng thái công nợ mới
        int compare = paidAmount.compareTo(debt.getTotalAmount());
        if (compare >= 0) {
            debt.setStatus(DebtStatus.PAID.getCode());
        } else if (paidAmount.compareTo(BigDecimal.ZERO) > 0) {
            debt.setStatus(DebtStatus.PARTIAL.getCode());
        } else {
            debt.setStatus(DebtStatus.UNPAID.getCode());
        }

        debtRepository.save(debt);
    }
}
