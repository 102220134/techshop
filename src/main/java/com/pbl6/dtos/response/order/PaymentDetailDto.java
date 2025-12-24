package com.pbl6.dtos.response.order;

import com.pbl6.enums.PaymentMethod;
import com.pbl6.enums.PaymentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentDetailDto {
    private Long id;
    private Long orderId;
    private BigDecimal amount;
    private PaymentMethod method;
    private PaymentStatus status;
    private String provider;
    private String transactionRef;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
}