package com.pbl6.dtos.request.auth;

import com.pbl6.utils.ValidationMessages;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
@Schema(description = "Request đăng nhập người dùng")
public class LoginRequest {

    @NotBlank(message = ValidationMessages.PHONE_REQUIRED)
    @Pattern(
            regexp = "^0[35789][0-9]{8}$",
            message = ValidationMessages.PHONE_INVALID
    )
    @Schema(description = "Số điện thoại đăng nhập", example = "0976912052", requiredMode = Schema.RequiredMode.REQUIRED)
    private String phone;

    @NotBlank(message = ValidationMessages.PASSWORD_REQUIRED)
    @Size(min = 6, max = 50, message = ValidationMessages.PASSWORD_TOO_SHORT)
    @Schema(description = "Mật khẩu đăng nhập", example = "123456789", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

}
