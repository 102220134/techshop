package com.pbl6.dtos.request.product;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO để lọc và phân trang sản phẩm")
public class ProductFilterRequest {

        @Schema(description = "Trường để sắp xếp kết quả", example = "id", allowableValues = {"id", "price", "create_at", "rating", "sold"})
        @Pattern(regexp = "id|price|create_at|rating|sold", message = "Giá trị 'order' không hợp lệ. Chỉ chấp nhận 'id', 'price', 'create_at', 'rating', 'sold'.")
        private String order = "id";

        @Schema(description = "Hướng sắp xếp (tăng dần hoặc giảm dần)", example = "asc", allowableValues = {"asc", "desc"})
        @Pattern(regexp = "asc|desc", message = "Giá trị 'dir' không hợp lệ. Chỉ chấp nhận 'asc' hoặc 'desc'.")
        private String dir = "asc";

        @Schema(description = "Số trang hiện tại", example = "1")
        @Min(value = 1, message = "Số trang phải lớn hơn hoặc bằng 1")
        private Integer page = 1;

        @Schema(description = "Số lượng sản phẩm trên mỗi trang", example = "20")
        @Min(value = 1, message = "Kích thước trang phải lớn hơn hoặc bằng 1")
        @Max(value = 100, message = "Kích thước trang không được vượt quá 100") // Example max size
        private Integer size = 20;

        @Schema(description = "Giá thấp nhất để lọc sản phẩm", example = "0.0")
        @DecimalMin(value = "0.0", message = "Giá từ phải là số không âm")
        private BigDecimal price_from = BigDecimal.valueOf(0);

        @Schema(description = "Giá cao nhất để lọc sản phẩm", example = "1000000000.0")
        @DecimalMin(value = "0.0", message = "Giá đến phải là số không âm")
        private BigDecimal price_to = BigDecimal.valueOf(1000000000);

        @Schema(description = "Bộ lọc thuộc tính sản phẩm", example = """
                    {
                    "mobile_nhu_cau_sd":"choi-game"
                    }
                    """)
        private Map<String, List<String>> filter = new HashMap<>();
}
