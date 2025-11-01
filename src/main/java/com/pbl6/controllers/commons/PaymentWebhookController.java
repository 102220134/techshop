package com.pbl6.controllers.commons;

import com.pbl6.dtos.request.webhook.SePayWebhookPayload;
import com.pbl6.dtos.response.ApiResponseDto;
import com.pbl6.exceptions.AppException;
import com.pbl6.exceptions.ErrorCode;
import com.pbl6.services.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/public/payment")
@RequiredArgsConstructor
public class PaymentWebhookController {

    @Value("${payment.bankTransfer.webhookSecret}")
    private String apiKey;

    private final PaymentService paymentService;

    @PostMapping("/webhook")
    public ApiResponseDto<String> handleWebhook(
            @RequestBody SePayWebhookPayload payload,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        log.info("Received SePay webhook: {}", payload);

        // 1️⃣ Verify Authorization header
        String expected = "Apikey " + apiKey;
        if (authHeader == null || !authHeader.equals(expected)) {
            log.warn("Unauthorized webhook request: {}", authHeader);
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        // 2️⃣ Gọi service xử lý
        String message = paymentService.handleSePayWebhook(payload);
        return new ApiResponseDto<>(message);
    }
}
