package com.pbl6.dtos.request.webhook;

import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;

// DTO Webhook
@Data
@ToString
public class SePayWebhookPayload {
    private Long id;
    private String gateway;
    private String transactionDate;
    private String accountNumber;
    private String code;
    private String content;
    private String transferType;
    private BigDecimal transferAmount;
    private BigDecimal accumulated;
    private String subAccount;
    private String referenceCode;
    private String description;
}
