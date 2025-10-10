package com.pbl6.dtos.request.checkout;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckoutShipRequest extends BaseCheckoutRequest {

    @NotBlank(message = "REQUIRED_FIELD_MISSING")
    private String line;

    @NotBlank(message = "REQUIRED_FIELD_MISSING")
    private String ward;

    @NotBlank(message = "REQUIRED_FIELD_MISSING")
    private String district;

    @NotBlank(message = "REQUIRED_FIELD_MISSING")
    private String province;
}
