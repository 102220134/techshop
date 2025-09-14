package com.pbl6.dtos.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;

import java.math.BigDecimal;
@Builder
public record ProductDto(
        Long id,
        String name,
        String description,
        String slug,
        String thumbnail,
        BigDecimal price,
        int stock,
        int reservedStock,
        int availableStock,
        @JsonIgnore
        int sold,
        @JsonIgnore
        double score,
        RatingSummary rating
) {
    public record RatingSummary(
            long total,
            double average
    ) {}
}
