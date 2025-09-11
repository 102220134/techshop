package com.pbl6.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class RefreshTokenRequest {
    @NotBlank(message = "REQUIRED_FIELD_MISSING")
    @NotNull(message = "REQUIRED_FIELD_MISSING")
    private String refreshToken;
}
