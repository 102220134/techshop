package com.pbl6.dtos.request.product;

import com.pbl6.utils.ValidationMessages;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
@Getter
@Setter
public class UpdateVariantRequest {
    private Long productId;

    @Schema(description = "Mã SKU (Stock Keeping Unit) của biến thể", example = "APP-IP16-PRO-MAX-VN-256GB-TI-DN-CH", requiredMode = Schema.RequiredMode.REQUIRED)
    private String sku;

    private Boolean isActive;

    @Schema(description = "Giá của biến thể sản phẩm", example = "39000000.00", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal price;

    @Schema(description = "File ảnh đại diện của biến thể", type = "string", format = "binary")
    private MultipartFile thumbnail;

    @Schema(description = "Danh sách các thuộc tính sản phẩm",
            example = "[{\"code\": \"color\", \"values\": [\"bac\"]}]")
    private String options;
}
