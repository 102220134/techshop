package com.pbl6.dtos.request.product;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.pbl6.enums.MediaType;
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
@Schema(description = "Request DTO để tạo một sản phẩm mới")
public class CreateProductRequest {
    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Schema(description = "Tên của sản phẩm", example = "iPhone 18 Pro Max")
    private String name;

    @Schema(description = "Mô tả chi tiết về sản phẩm", example = "Áo thun cotton 100%, thoáng mát, phù hợp cho mùa hè.")
    private String description;

    @Schema(description = "Thông tin chi tiết sản phẩm dưới dạng JSON", example = "{\"weight\": \"30461.0000\", \"gpu\": \"5‑core GPU\"}")
    private ObjectNode detail;

    @NotBlank(message = "Slug không được để trống")
    @Schema(description = "Slug (URL thân thiện) của sản phẩm", example = "iphone-18-pro-max")
    private String slug;

    // File ảnh đại diện
    @Schema(description = "File ảnh đại diện của sản phẩm", type = "string", format = "binary")
    private MultipartFile thumbnail;

    // Danh mục
    @NotNull(message = "Danh mục không được để trống")
    @NotEmpty(message = "Danh mục không được để trống")
    @Schema(description = "Danh sách ID của các danh mục mà sản phẩm thuộc về", example = "[3, 132]")
    private List<Long> categoryIds;

    // File media (ảnh/video khác)
    @Valid
    @Schema(description = "Danh sách các file media (ảnh/video) khác của sản phẩm")
    private List<MediaRequest> medias;

    @NotNull(message = "Biến thể sản phẩm không được để trống")
    @NotEmpty(message = "Biến thể sản phẩm không được để trống")
    @Valid
    @Schema(description = "Danh sách các biến thể của sản phẩm")
    private List<CreateVariantRequest> variants;

    @Valid
    @Schema(description = "Danh sách các thuộc tính sản phẩm (ví dụ: Phiên bản)")
    private List<AttributeRequest> filters;

    @Schema(description = "ID của sản phẩm anh em (nếu có), dùng cho sản phẩm có nhiều phiên bản chính", example = "123")
    private Long sibling;


    @Schema(description = "Tên liên quan ( tên phiên bản )", example = "512GB")
    private String relatedName;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Request DTO cho một file media của sản phẩm")
    public static class MediaRequest {
        @NotNull(message = "File media không được để trống")
        @Schema(description = "File media (ảnh hoặc video)")
        private MultipartFile file;

        @NotBlank(message = "Loại media không được để trống")
        @Pattern(regexp = "image|video", message = "Loại media phải là 'image' hoặc 'video'")
        @Schema(description = "Loại của media (image hoặc video)", allowableValues = {"image", "video"}, example = "image")
        private MediaType type; // image / video

        @Min(value = 0, message = "Thứ tự sắp xếp phải là số không âm")
        @Schema(description = "Thứ tự sắp xếp của media", example = "0")
        private Integer sortOrder;

        @Schema(description = "Văn bản thay thế cho media (alt text)", example = "Ảnh sản phẩm áo thun")
        private String altText;
    }
}
