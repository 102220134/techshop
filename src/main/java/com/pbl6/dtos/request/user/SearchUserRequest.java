package com.pbl6.dtos.request.user;

import com.pbl6.enums.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SearchUserRequest {
    Integer size = 20;
    Integer page = 1;
    String name;
    Boolean isActive;

    @Pattern(
            regexp = "^0[0-9]{0,9}$",
            message = "INVALID_PHONE_FORMAT"
    )

    private String phone;
    @Schema(allowableValues = {"create_at", "total_amount_spent","total_orders"})
    String order = "create_at";
    @Schema(allowableValues = {"asc", "desc"})
    String dir ="desc";
}
