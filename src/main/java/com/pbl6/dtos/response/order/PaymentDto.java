package com.pbl6.dtos.response.order;

import com.pbl6.enums.PaymentMethod;
import com.pbl6.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDto {
    Long id;
    PaymentMethod method;
    PaymentStatus status;
    BigDecimal amount;
    String provider;
    String transactionRef;
    LocalDateTime paidAt;
}
