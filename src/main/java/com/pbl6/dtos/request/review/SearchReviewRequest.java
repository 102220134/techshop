package com.pbl6.dtos.request.review;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Request tìm kiếm đánh giá sản phẩm")
public class SearchReviewRequest {

    @Schema(description = "Số lượng bản ghi mỗi trang", example = "10", defaultValue = "10")
    @Min(value = 1, message = "Size phải >= 1")
    private Integer size = 10;

    @Schema(description = "Trang hiện tại (bắt đầu từ 1)", example = "1", defaultValue = "1")
    @Min(value = 1, message = "Page phải >= 1")
    private Integer page = 1;

    @Schema(description = "ID sản phẩm cần xem đánh giá", example = "12345", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "productId không được để trống")
    private Long productId;

    @Schema(description = "Lọc theo số sao (1-5). Nếu bỏ trống thì lấy tất cả.", example = "5")
    @Min(value = 1, message = ">=1")
    @Max(value = 5,message = "<=5")
    private Short rating;
}
