package com.pbl6.services;

import com.pbl6.dtos.response.VariantDto;

import java.util.List;
import java.util.Map;

public interface VariantService {
    List<VariantDto> getVariantsByProduct(Long productId, Long warehouseId);
    Map<Long, List<VariantDto.WarehouseStockDto>> getWarehouseStockForVariants(List<Long> variantIds);
}
