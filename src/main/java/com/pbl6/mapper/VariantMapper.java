package com.pbl6.mapper;

import com.pbl6.dtos.response.product.VariantDto;
import com.pbl6.entities.VariantEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class VariantMapper {

    public List<VariantDto> toDtoList(Set<VariantEntity> variants) {
        if (variants == null) return List.of();

        return variants.stream()
                .filter(v -> Boolean.TRUE.equals(v.getIsActive()))
                .map(this::toDto)
                .toList();
    }

    public VariantDto toDto(VariantEntity v) {
        return VariantDto.builder()
                .id(v.getId())
                .sku(v.getSku())
                .thumbnail(v.getThumbnail())
                .price(v.getPrice())
                .specialPrice(v.getDiscountedPrice())
                .availableStock(v.getAvailableStock())
                .stock(v.getStock())
                .attributes(
                        v.getVariantAttributeValues().stream()
                                .map(vav -> VariantDto.AttributeDto.builder()
                                        .code(vav.getAttribute().getCode())
                                        .label(vav.getAttribute().getLabel())
                                        .value(vav.getAttributeValue().getLabel())
                                        .build())
                                .toList()
                )
                .build();
    }
}

