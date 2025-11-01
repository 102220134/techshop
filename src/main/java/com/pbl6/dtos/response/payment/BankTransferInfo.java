package com.pbl6.dtos.response.payment;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.pbl6.enums.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Thông tin chi tiết nếu phương thức thanh toán là chuyển khoản ngân hàng.
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BankTransferInfo extends PaymentInfo {
    private PaymentMethod type;
    private String label;
    private String bankAccountNumber;
    private String bankAccountName;
    private String bankName;
    private String transferContent;
    private String qrCodeUrl;
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime lifeTime;
}
