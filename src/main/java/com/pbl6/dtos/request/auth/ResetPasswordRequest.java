package com.pbl6.dtos.request.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class ResetPasswordRequest {
    @NotNull(message = "REQUIRED_FIELD_MISSING")
    @Email(message = "INVALID_EMAIL_FORMAT")
    private String email;
}
