package com.pbl6.dtos.request;


import jakarta.validation.constraints.*;
import lombok.Getter;

@Getter
public class RegisterRequest {
    @NotBlank(message = "REQUIRED_FIELD_MISSING")
    @NotNull(message = "REQUIRED_FIELD_MISSING")
    private String name;

    @NotNull(message = "REQUIRED_FIELD_MISSING")
    @Email(message = "INVALID_EMAIL_FORMAT")
    private String email;

    @NotNull(message = "REQUIRED_FIELD_MISSING")
    @Size(min = 6, message = "INVALID_PASSWORD_FORMAT")
    private String password;

    @Pattern(
            regexp = "^$|^(0[1-9][0-9]{8})$",
            message = "INVALID_PHONE_FORMAT"
    )

    @NotBlank(message = "REQUIRED_FIELD_MISSING")
    @NotNull(message = "REQUIRED_FIELD_MISSING")
    private String phone;

}
