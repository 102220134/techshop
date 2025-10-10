package com.pbl6.dtos.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record VariantDto(
        long id,
        String sku,
        String thumbnail,
        BigDecimal price,
        BigDecimal specialPrice,
        List<AttributeDto> attributes,
        int availableStock
) {
    @Builder
    public record AttributeDto(
            String code,
            String label,
            String value
    ) { }
}
