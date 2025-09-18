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
        BigDecimal special_price,
        List<AttributeDto> attributes,
        StockDto stock
) {
    @Builder
    public record AttributeDto(
            String code,
            String label,
            String value
    ) { }
    @Builder
    public record StockDto (
            Long warehouseId,
            int availableStock,
            List<WarehouseStockDto> otherWarehouses
    ) { }
    @Builder
    public record WarehouseStockDto(
             Long warehouseId,
             String name,
             int availableStock
    ){}
}
