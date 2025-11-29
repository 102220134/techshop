package com.pbl6.dtos.response.inventory.delivery;

import com.pbl6.dtos.response.product.VariantDto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
@Builder
public class DeliveryItemDto {
    private long variantId;
    private String sku;
    private String thumbnail;
    private List<VariantDto.AttributeDto> attributes;
    private Integer quantity;
}
