package com.pbl6.dtos.request.checkout;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckoutPickupRequest extends BaseCheckoutRequest {

    @NotNull(message = "REQUIRED_FIELD_MISSING")
    private Long storeId;
}
