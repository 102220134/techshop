package com.pbl6.dtos.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Schema(
        name = "ProductSearchRequest",
        description = """
        Body filter cho API tìm kiếm sản phẩm. 
        FE có thể truyền nhiều category, nhiều filters theo attribute code.
        Nếu để trống minPrice/maxPrice thì không lọc theo giá.
        """
)
public record ProductSearchRequest(

        @Schema(
                description = "Danh sách slug của category nếu truyền nhiều thì lấy OR",
                defaultValue = "[\"apple\"]"
        )
        List<String> categories,

        @Schema(
                description = "Filter theo attribute code : values",
                defaultValue = """
                {
                  "mobile_tan_so_quet": ["120Hz"],
                  "mobile_nhu_cau_sd": ["Chơi game","Livestream"],
                  "mobile_storage_filter": ["Trên 512GB"]
                }
                """
        )
        Map<String, List<String>> filters,

        @Schema(defaultValue = "0")
        BigDecimal minPrice,

        @Schema(defaultValue = "50000000")
        BigDecimal maxPrice,

        @NotNull(message = "REQUIRED_FIELD_MISSING")
        @Schema(description = "ID kho hàng để lọc tồn kho. Không được trống", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        Long warehouseId,

        @Schema( defaultValue = "0")
        Integer page,

        @Schema( defaultValue = "20")
        Integer size,

        @Schema(
                example = "[\"price,asc\",\"createdAt,desc\"]",
                defaultValue = "[\"price,desc\"]"
        )
        List<String> sort
) {}
