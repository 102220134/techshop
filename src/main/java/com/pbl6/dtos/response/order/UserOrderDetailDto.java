package com.pbl6.dtos.response.order;

import com.pbl6.enums.OrderStatus;
import com.pbl6.enums.PaymentMethod;
import com.pbl6.enums.ReceiveMethod;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserOrderDetailDto {
    private Long id;
    private OrderStatus status;
    private BigDecimal grossAmount;
    private BigDecimal directDiscount;
    private BigDecimal voucherDiscount;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal remainingAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private PaymentMethod paymentMethod;
    private ReceiveMethod receiveMethod;
    private String receiverAddress;
    private String receiverName;
    private String receiverPhone;
    private List<OrderItemDto> items;
}
