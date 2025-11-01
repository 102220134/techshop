package com.pbl6.dtos.response.order;

import com.pbl6.dtos.response.product.VariantDto;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record OrderItemDto(
        long id,
        String name,
        String sku,
        String thumbnail,
        BigDecimal price,
        BigDecimal finalPrice,
        int quantity,
        BigDecimal subtotal,
        List<VariantDto.AttributeDto> attributes
) { }