package com.pbl6.dtos.request.checkout;

import com.pbl6.dtos.request.order.OrderItemRequest;
import com.pbl6.enums.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public abstract class BaseCheckoutRequest {

    @NotEmpty(message = "REQUIRED_FIELD_MISSING")
    @Valid
    private List<OrderItemRequest> orderItems;

    @NotBlank(message = "REQUIRED_FIELD_MISSING")
    private String fullName;

    @NotBlank(message = "REQUIRED_FIELD_MISSING")
    @Pattern(
            regexp = "^0[35789][0-9]{8}$",
            message = "INVALID_PHONE_FORMAT")
    private String phone;

    @Email(message = "INVALID_EMAIL_FORMAT")
    private String email;

    @NotNull(message = "REQUIRED_FIELD_MISSING")
    private PaymentMethod paymentMethod;

}
