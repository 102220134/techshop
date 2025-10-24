package com.pbl6.dtos.request.product;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request DTO cho một cặp thuộc tính và giá trị của biến thể")
public class AttributeRequest {
    @NotNull(message = "Attribute  không được để trống")
    @NotBlank(message = "Attribute  không được để trống")
    private String code;

    @NotNull(message = "Value không được để trống")
    @NotEmpty(message = "Value không được để trống")
    private String value;
}
