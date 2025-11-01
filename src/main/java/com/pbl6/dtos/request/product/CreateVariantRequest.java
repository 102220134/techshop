package com.pbl6.dtos.request.product;

import com.pbl6.utils.ValidationMessages;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request DTO để tạo một biến thể sản phẩm mới")
public class CreateVariantRequest {
    
    @NotBlank(message = ValidationMessages.PRODUCT_SKU_REQUIRED)
    @Size(min = 1, max = 100, message = ValidationMessages.PRODUCT_SKU_LENGTH)
    @Schema(description = "Mã SKU (Stock Keeping Unit) của biến thể", example = "APP-IP16-PRO-MAX-VN-256GB-TI-DN-CH", requiredMode = Schema.RequiredMode.REQUIRED)
    private String sku;

    @NotNull(message = ValidationMessages.PRODUCT_PRICE_REQUIRED)
    @DecimalMin(value = "0.0", inclusive = false, message = ValidationMessages.PRODUCT_PRICE_POSITIVE)
    @Schema(description = "Giá của biến thể sản phẩm", example = "39000000.00", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal price;

    @Schema(description = "File ảnh đại diện của biến thể", type = "string", format = "binary")
    private MultipartFile thumbnail;

    @NotNull(message = ValidationMessages.PRODUCT_ATTRIBUTE_REQUIRED)
    @NotEmpty(message = ValidationMessages.PRODUCT_ATTRIBUTE_REQUIRED)
    @Valid
    @Schema(description = "Danh sách các thuộc tính và giá trị của biến thể (ví dụ: màu sắc)", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<AttributeRequest> options;

}
