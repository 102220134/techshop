package com.pbl6.dtos.request.auth;

import com.pbl6.utils.ValidationMessages;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
@Schema(description = "Request làm mới token")
public class RefreshTokenRequest {
    
    @NotBlank(message = ValidationMessages.TOKEN_REQUIRED)
    @Schema(description = "Refresh token để làm mới access token", requiredMode = Schema.RequiredMode.REQUIRED)
    private String refreshToken;
}
