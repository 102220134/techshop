package com.pbl6.dtos.request.product;

import com.pbl6.utils.ValidationMessages;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request DTO cho một cặp thuộc tính và giá trị của biến thể")
public class AttributeRequest {
    
    @NotBlank(message = ValidationMessages.PRODUCT_ATTRIBUTE_CODE_REQUIRED)
    @Size(min = 1, max = 50, message = "Mã thuộc tính phải có từ 1-50 ký tự")
    @Schema(description = "Mã thuộc tính", example = "color", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;

    @NotBlank(message = ValidationMessages.PRODUCT_ATTRIBUTE_VALUE_REQUIRED)
    @Size(min = 1, max = 100, message = "Giá trị thuộc tính phải có từ 1-100 ký tự")
    @Schema(description = "Giá trị thuộc tính", example = "Đỏ", requiredMode = Schema.RequiredMode.REQUIRED)
    private String value;
}
