package com.pbl6.dtos.request.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import java.time.LocalDate;

@Data
@Schema(description = "Thông tin cập nhật hồ sơ người dùng")
public class UserUpdateInfoRequest {

    @Schema(description = "Tên người dùng")
    @NotBlank(message = "REQUIRED_FIELD_MISSING")
    @NotNull(message = "REQUIRED_FIELD_MISSING")
    private String name;

    @Schema(description = "Giới tính (Nam/Nữ)")
    @NotBlank(message = "REQUIRED_FIELD_MISSING")
    @Pattern(regexp = "^(Nam|Nữ)$", message = "GENDER_INVALID")
    private String gender;

    @Schema(description = "Ngày sinh")
    @NotBlank(message = "REQUIRED_FIELD_MISSING")
    @NotNull(message = "REQUIRED_FIELD_MISSING")
    private LocalDate birth;
}
