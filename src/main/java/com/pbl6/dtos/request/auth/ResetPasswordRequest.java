package com.pbl6.dtos.request.auth;

import com.pbl6.utils.ValidationMessages;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
@Schema(description = "Request reset mật khẩu")
public class ResetPasswordRequest {
    
    @NotBlank(message = ValidationMessages.EMAIL_REQUIRED)
    @Email(message = ValidationMessages.EMAIL_INVALID)
    @Schema(description = "Email để reset mật khẩu", example = "nguyenvana@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;
}
