package com.pbl6.dtos.response.product;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.pbl6.dtos.response.BreadcrumbDto;
import com.pbl6.dtos.response.promotion.PromotionDto;
import lombok.*;

import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@JsonPropertyOrder({
        "id",
        "name",
        "description",
        "slug",
        "thumbnail",
        "price",
        "special_price",
        "promotions",
        "available",
        "detail",
        "attributes",
        "variants",
        "medias",
        "siblings",
        "rating",
        "breadcrumb"
})
public class ProductDetailDto {

    private long id;
    private String name;
    private String description;
    private String slug;
    private String thumbnail;

    @JsonProperty("price")
    private Double price; // nếu bạn có giá base

    @JsonProperty("special_price")
    private Double specialPrice; // nếu có giá khuyến mãi

    private List<PromotionDto> promotions;

    private boolean isAvailable;
    private ObjectNode detail;

    /** 🔹 Danh sách thuộc tính hiển thị (attribute name, value) */
//    private List<AttributeDto> attributes;

    /** 🔹 Danh sách biến thể (màu, dung lượng, v.v.) */
    private List<VariantDto> variants;

    /** 🔹 Ảnh, video... */
    private List<MediaDto> medias;

    /** 🔹 Sản phẩm liên quan (sibling) */
    private List<SiblingDto> siblings;

    private RatingSummary rating;

    private BreadcrumbDto breadcrumb;

    // ==========================
    // Inner DTOs
    // ==========================

    @AllArgsConstructor
    @Getter
    public static class RatingSummary {
        private long total;
        private double average;
    }

//    @AllArgsConstructor
//    @NoArgsConstructor
//    @Builder
//    @Getter
//    @Setter
//    public static class AttributeDto {
//        private Long id;
//        private String code;
//        private String label;
//        private String value;
//    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Getter
    @Setter
    public static class SiblingDto {
        private Long id;
        private String name;
        private String slug;
        private String related_name;
        private String thumbnail;
    }
}
