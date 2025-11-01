package com.pbl6.dtos.response;

import com.pbl6.dtos.response.payment.PaymentInfo;
import com.pbl6.dtos.response.payment.PaymentInitResponse;
import com.pbl6.enums.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Thông tin chi tiết nếu thanh toán qua VNPAY.
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VnPayInfo extends PaymentInfo {
    private PaymentMethod type;
    private String label;
    private String paymentUrl;
    private String transactionCode;
    private LocalDateTime expireAt;
}
