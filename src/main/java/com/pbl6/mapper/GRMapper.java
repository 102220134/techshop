package com.pbl6.mapper;

import com.pbl6.dtos.response.inventory.GR.GRDto;
import com.pbl6.dtos.response.inventory.GR.GRItemDto;
import com.pbl6.dtos.response.inventory.SupplierDto;
import com.pbl6.dtos.response.product.VariantDto;
import com.pbl6.entities.GoodsReceiptEntity;
import com.pbl6.entities.GoodsReceiptItemEntity;
import com.pbl6.entities.VariantEntity;
import org.springframework.stereotype.Component;

@Component
public class GRMapper {
    public GRDto toDto(GoodsReceiptEntity e){
        SupplierDto supplierDto = SupplierDto.builder()
                .id(e.getSupplier().getId())
                .name(e.getSupplier().getName())
                .displayAddress(e.getSupplier().getAddress())
                .email(e.getSupplier().getEmail())
                .taxCode(e.getSupplier().getTaxCode())
                .build();
        return GRDto.builder()
                .id(e.getId())
                .supplier(supplierDto)
                .status(e.getStatus())
                .note(e.getNote())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
    public GRItemDto grItemDto(GoodsReceiptItemEntity e){
        VariantEntity variant = e.getVariant();
        return GRItemDto.builder()
                .id(e.getId())
                .variantId(variant.getId())
                .sku(variant.getSku())
                .thumbnail(variant.getThumbnail())
                .quantity(e.getQuantity())
                .unitCost(e.getUnitCost())
                .attributes(
                        variant.getVariantAttributeValues().stream()
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
