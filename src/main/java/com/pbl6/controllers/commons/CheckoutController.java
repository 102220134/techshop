package com.pbl6.controllers.commons;

import com.pbl6.dtos.request.checkout.CheckoutPickupRequest;
import com.pbl6.dtos.request.checkout.CheckoutShipRequest;
import com.pbl6.dtos.response.ApiResponseDto;
import com.pbl6.dtos.response.payment.PaymentInitResponse;
import com.pbl6.entities.UserEntity;
import com.pbl6.exceptions.AppException;
import com.pbl6.exceptions.ErrorCode;
import com.pbl6.services.CheckoutService;
import com.pbl6.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/checkout")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Thanh toán",description = "Nếu guest thì điền name, sdt ,email(tuỳ) không cần jwt. Nếu đã login thì điền từ infor của user và có security = jwt")
public class CheckoutController {

    private final CheckoutService checkoutService;
    private final UserService userService;

    // 🚚 1️⃣ API cho giao hàng tận nơi
    @PostMapping("/ship")
    @Operation(summary = "Giao hàng tận nơi", security = { @SecurityRequirement(name = "bearerAuth") })
    public ApiResponseDto<PaymentInitResponse> checkoutShip(@Valid @RequestBody CheckoutShipRequest req) {
        UserEntity user = resolveUser(req.getEmail(), req.getPhone(), req.getFullName());
        PaymentInitResponse paymentInitResponse = checkoutService.processCheckoutShipment(user, req);
        ApiResponseDto<PaymentInitResponse> response = new ApiResponseDto<>();
        response.setData(paymentInitResponse);
        return response;
    }

    // 🏬 2️⃣ API cho nhận hàng tại cửa hàng
    @PostMapping("/pickup")
    @Operation(summary = "Nhận tại cửa hàng", security = { @SecurityRequirement(name = "bearerAuth") })
    public ApiResponseDto<PaymentInitResponse> checkoutPickup(@Valid @RequestBody CheckoutPickupRequest req) {
        UserEntity user = resolveUser(req.getEmail(), req.getPhone(), req.getFullName());

        PaymentInitResponse paymentInitResponse = checkoutService.processCheckoutPickup(user, req);
        ApiResponseDto<PaymentInitResponse> response = new ApiResponseDto<>();
        response.setData(paymentInitResponse);
        return response;
    }

    // 🔐 Helper chung cho cả hai API
    private UserEntity resolveUser(String email, String phone, String fullName) {
        var auth = SecurityContextHolder.getContext().getAuthentication();

        // Nếu có user login
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            Object principal = auth.getPrincipal();
            if (principal instanceof UserEntity userEntity) {
                return userEntity;
            } else {
                log.error("Principal không phải UserEntity, kiểu: {}", principal.getClass());
                throw new AppException(ErrorCode.UNAUTHORIZED);
            }
        }
        return userService.createOrGetGuest(email, phone, fullName);
    }
}
