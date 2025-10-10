package com.pbl6.dtos.request.product;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
public class AddToCartRequest {

    @NotNull(message = "REQUIRED_FIELD_MISSING")
    Long variantId;

    @NotNull(message = "REQUIRED_FIELD_MISSING")
    @Min(value = 1, message = "INVALID_FIELD_FORMAT")
    Integer quantity;
}
