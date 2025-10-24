package com.pbl6.dtos.request.product;

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
    @NotBlank(message = "Mã SKU không được để trống")
    @Schema(description = "Mã SKU (Stock Keeping Unit) của biến thể", example = "APP-IP16-PRO-MAX-VN-256GB-TI-DN-CH")
    private String sku;

    @NotNull(message = "Giá sản phẩm không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá sản phẩm phải lớn hơn 0")
    @Schema(description = "Giá của biến thể sản phẩm", example = "39000000.00")
    private BigDecimal price;

    @Schema(description = "File ảnh đại diện của biến thể", type = "string", format = "binary")
    private MultipartFile thumbnail;

    @NotNull(message = "Thuộc tính biến thể không được để trống")
    @NotEmpty(message = "Thuộc tính biến thể không được để trống")
    @Valid
    @Schema(description = "Danh sách các thuộc tính và giá trị của biến thể (ví dụ: màu sắc)")
    private List<AttributeRequest> options;

}
