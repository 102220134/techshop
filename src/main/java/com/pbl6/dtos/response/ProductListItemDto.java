package com.pbl6.dtos.response;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.math.BigDecimal;

public record ProductListItemDto(
        Long id,
        String name,
        String description,
        ObjectNode detail,
        String slug,
        String thumbnail,
        BigDecimal price,
        Integer stock,
        RatingSummary rating
) {
    public static record RatingSummary(
            long total,
            long star1,
            long star2,
            long star3,
            long star4,
            long star5,
            BigDecimal average
    ) {}
}
