package com.pbl6.dtos.request.product;

import com.pbl6.utils.ValidationMessages;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request DTO cho một cặp thuộc tính và giá trị của biến thể")
public class AttributeRequest {
    
    @NotBlank(message = ValidationMessages.PRODUCT_ATTRIBUTE_CODE_REQUIRED)
    private String code;

    @NotBlank(message = ValidationMessages.PRODUCT_ATTRIBUTE_VALUE_REQUIRED)
    private List<String> values;
}
