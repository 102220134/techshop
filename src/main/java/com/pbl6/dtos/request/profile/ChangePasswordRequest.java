package com.pbl6.dtos.request.profile;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Request đổi mật khẩu người dùng")
public class ChangePasswordRequest {
    @Schema(description = "Mật khẩu hiện tại", example = "123456")
    @NotBlank(message = "REQUIRED_FIELD_MISSING")
    private String oldPassword;

    @Schema(description = "Mật khẩu mới", example = "abc@123")
    @NotBlank(message = "REQUIRED_FIELD_MISSING")
    @Size(min = 6, message = "PASSWORD_TOO_SHORT") // Assuming a minimum password length of 6
    private String newPassword;

    @Schema(description = "Xác nhận mật khẩu mới", example = "abc@123")
    @NotBlank(message = "REQUIRED_FIELD_MISSING")
    @Size(min = 6, message = "PASSWORD_TOO_SHORT") // Assuming a minimum password length of 6
    private String confirmPassword;
}

