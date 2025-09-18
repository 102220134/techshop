package com.pbl6.dtos.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
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
        PromotionDto promotion,
        RatingSummary rating,
        @JsonIgnore
        int sold,
        @JsonIgnore
        double score
) {
    public record RatingSummary(
            long total,
            double average
    ) {}
}
