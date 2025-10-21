package com.pbl6.dtos.response.product;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pbl6.dtos.response.promotion.PromotionDto;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record ProductDto(
        long id,
        String name,
        String description,
        String slug,
        String thumbnail,
        BigDecimal price,
        BigDecimal special_price,
        int stock,
        int reserved_stock,
        int available_stock,
        List<PromotionDto> promotions,
        RatingSummary rating
) {
    public record RatingSummary(
            long total,
            double average
    ) {}
}
