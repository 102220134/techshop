package com.pbl6.services;

import com.pbl6.dtos.response.VariantDto;

import java.util.List;

public interface VariantService {
    List<VariantDto> getVariantsByProduct(Long productId, Long warehouseId);
}
