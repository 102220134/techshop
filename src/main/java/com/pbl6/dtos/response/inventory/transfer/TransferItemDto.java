package com.pbl6.dtos.response.inventory.transfer;

import com.pbl6.dtos.response.product.VariantDto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
public class TransferItemDto {
    private long id;
    private long variantId;
    private String sku;
    private String thumbnail;
    private List<VariantDto.AttributeDto> attributes;
    private Integer quantity;
}
