package com.pbl6.dtos.response.product;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Builder
@Setter
@Getter
public record VariantDto(
        long id,
        String sku,
        String thumbnail,
        BigDecimal price,
        BigDecimal specialPrice,
        List<AttributeDto> attributes,
        int availableStock,
        int stock
) {
    @Builder
    public record AttributeDto(
            String code,
            String label,
            String value
    ) { }
}
