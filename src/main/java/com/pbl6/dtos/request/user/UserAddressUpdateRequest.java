package com.pbl6.dtos.request.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Request cập nhật địa chỉ người dùng")
public class UserAddressUpdateRequest {

    @Schema(description = "ID địa chỉ cần cập nhật")
    @NotNull(message = "REQUIRED_FIELD_MISSING")
    private Long id;

    @Schema(description = "Địa chỉ cụ thể (số nhà, đường, ...)")
    @NotBlank(message = "REQUIRED_FIELD_MISSING")
    private String line;

    @Schema(description = "Phường/Xã")
    @NotBlank(message = "REQUIRED_FIELD_MISSING")
    private String ward;

    @Schema(description = "Quận/Huyện")
    @NotBlank(message = "REQUIRED_FIELD_MISSING")
    private String district;

    @Schema(description = "Tỉnh/Thành phố")
    @NotBlank(message = "REQUIRED_FIELD_MISSING")
    private String province;

    @Schema(description = "Đặt làm địa chỉ mặc định?")
    private Boolean isDefault = false;
}

