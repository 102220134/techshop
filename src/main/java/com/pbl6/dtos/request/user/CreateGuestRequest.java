package com.pbl6.dtos.request.user;

import com.pbl6.utils.ValidationMessages;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateGuestRequest {
    @NotBlank(message = ValidationMessages.NAME_REQUIRED)
    @Size(min = 2, max = 100, message = ValidationMessages.NAME_LENGTH)
    @Schema(description = "Họ tên người dùng", example = "Nguyễn Văn A", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @NotBlank(message = ValidationMessages.EMAIL_REQUIRED)
    @Email(message = ValidationMessages.EMAIL_INVALID)
    @Schema(description = "Email người dùng", example = "nguyenvana@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotBlank(message = ValidationMessages.PHONE_REQUIRED)
    @Pattern(
            regexp = "^0[35789][0-9]{8}$",
            message = ValidationMessages.PHONE_INVALID
    )
    @Schema(description = "Số điện thoại", example = "0987654321", requiredMode = Schema.RequiredMode.REQUIRED)
    private String phone;
}
