package com.pbl6.dtos.response.product;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
        "promotion",
        "available",
        "detail",
        "variants",
        "medias",
        "rating"
})

public class ProductDetailDto {
    private long id;
    private String name;
    private String description;
    private String slug;
    private String thumbnail;
    private List<PromotionDto> promotions;
    private ObjectNode detail;
    private boolean isAvailable;
    private List<VariantDto> variants;
    private List<MediaDto> medias;
    private RatingSummary rating;

    @AllArgsConstructor
    @Getter
    public static class RatingSummary {
        private long total;
        private double average;
    }
}
