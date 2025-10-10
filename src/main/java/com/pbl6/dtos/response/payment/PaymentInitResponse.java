package com.pbl6.dtos.response.payment;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO trả về cho FE sau khi khởi tạo thanh toán (checkout)
 * Dùng chung cho mọi phương thức: banking / ví / COD / trả góp
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentInitResponse {
    private Long orderId;
    private BigDecimal amount;
    PaymentInfo paymentInfo;
    private String message;

}
