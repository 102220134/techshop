package com.pbl6.dtos.request.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class LoginRequest {

    @Pattern(
            regexp = "^$|^(0[1-9][0-9]{8})$",
            message = "INVALID_PHONE_FORMAT"
    )

    @NotBlank(message = "REQUIRED_FIELD_MISSING")
    @NotNull(message = "REQUIRED_FIELD_MISSING")
    private String phone;

    @NotNull(message = "REQUIRED_FIELD_MISSING")
    @Size(min = 6, message = "INVALID_PASSWORD_FORMAT")
    private String password;

}
