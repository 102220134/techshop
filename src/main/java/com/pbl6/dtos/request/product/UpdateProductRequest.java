package com.pbl6.dtos.request.product;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Schema(description = "Update thông tin cơ bản của product")
public class UpdateProductRequest {
    @Schema(description = "Tên của sản phẩm", example = "iPhone 18 Pro Max")
    private String name;


    @Schema(description = "Mô tả chi tiết về sản phẩm", example = "iPhone 17 Pro Max có gì mới? Khám phá thiết kế mỏng nhẹ, màn hình 144Hz cho mọi phiên bản")
    private String description;

    @Schema(description = "Thông tin chi tiết sản phẩm dưới dạng JSON", example = "{\"weight\": \"30461.0000\", \"gpu\": \"5‑core GPU\"}")
    private ObjectNode detail;

    @Schema(description = "Slug (URL thân thiện) của sản phẩm", example = "iphone-18-pro-max")
    private String slug;

    @Schema(description = "File ảnh đại diện của sản phẩm", type = "string", format = "binary")
    private MultipartFile thumbnail;

    @Schema(description = "Danh sách ID của các danh mục mà sản phẩm thuộc về", example = "[3, 132]")
    private List<Long> categoryIds;

    @Schema(description = "ID của sản phẩm anh em (nếu có), dùng cho sản phẩm có nhiều phiên bản chính", example = "123")
    private Long sibling;

    @Schema(description = "Tên liên quan ( tên phiên bản )", example = "512GB")
    private String relatedName;

    @Valid
    @Schema(description = "Danh sách các filter")
    private List<AttributeRequest> filters;
}
